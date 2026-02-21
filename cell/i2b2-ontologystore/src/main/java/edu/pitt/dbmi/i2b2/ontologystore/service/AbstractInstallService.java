/*
 * Copyright (C) 2024 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.i2b2.ontologystore.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

/**
 *
 * Jan 19, 2024 9:36:38 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractInstallService {

    private static final Log LOGGER = LogFactory.getLog(AbstractInstallService.class);

    protected static final String ACTION_TYPE = "Install";

    protected static final String YYYYMMDD_REGX = "^\\d{4}/(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])$";
    protected static final String DDMMMYY_REGX = "^(0[1-9]|[12][0-9]|3[01])-(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)-(\\d{2})$";
    protected static final String YYYYMMDD_DASH_REGX = "^\\d{4}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";

    protected static final Pattern TAB_DELIM = Pattern.compile("\t");
    protected static final Pattern YYYYMMDD_PATTERN = Pattern.compile(YYYYMMDD_REGX);
    protected static final Pattern DDMMMYY_PATTERN = Pattern.compile(DDMMMYY_REGX);
    protected static final Pattern YYYYMMDD_DASH_PATTERN = Pattern.compile(YYYYMMDD_DASH_REGX);

    protected static final DateFormat YYYYMMDD_DF = new SimpleDateFormat("yyyy/mm/dd");
    protected static final DateFormat DDMMMYY_DF = new SimpleDateFormat("dd-MMM-yy");
    protected static final DateFormat YYYYMMDD_DASH_DF = new SimpleDateFormat("yyyy-mm-dd");

    protected static final int DEFAULT_BATCH_SIZE = 50;

    protected final FileSystemService fileSystemService;

    public AbstractInstallService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    protected void deleteFromTableAccess(JdbcTemplate jdbcTemplate, String table, String columnName, List<String> tableNames) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            tableNames.forEach(tableName -> {
                try {
                    String sql = createDeleteStatement(conn.getSchema(), table.toLowerCase(), columnName);
                    PreparedStatement stmt = conn.prepareStatement(sql);

                    stmt.setString(1, tableName);
                    stmt.execute();
                } catch (Exception exception) {
                    LOGGER.error("", exception);
                }
            });
        }
    }

    protected void batchInsertMetadata(JdbcTemplate jdbcTemplate, String table, ZipEntry zipEntry, ZipFile zipFile, int batchSize) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
            // create prepared statement
            String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), getHeaders(reader.readLine()));
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());

                int count = 0;
                final int C_FACTTABLECOLUMN = 9;
                final int C_OPERATOR = 13;
                final int C_FACTTABLECOLUMN_INDEX = 8;
                final int C_OPERATOR_INDEX = 12;
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    // skip lines that are commented out
                    String cleanedLine = line.trim();
                    if (cleanedLine.isEmpty() || cleanedLine.startsWith("--")) {
                        continue;
                    }

                    try {
                        // add dummy value ($) at the beganing and end of the line before splitting
                        String[] temp = TAB_DELIM.split(String.format("$\t%s\t$", line));

                        // create a new array of data without the dummy values
                        String[] values = new String[temp.length - 2];
                        System.arraycopy(temp, 1, values, 0, values.length);

                        // trim each element in-place
                        Arrays.setAll(values, i -> values[i].trim());

                        setColumns(stmt, columnTypes, values);

                        // add null columns not provided
                        if (values.length < columnTypes.length) {
                            for (int i = values.length + 1; i <= columnTypes.length; i++) {
                                stmt.setNull(i, Types.NULL);
                            }
                        }

                        // ensure not-null constraint is satisfied
                        if (values[C_FACTTABLECOLUMN_INDEX].isEmpty()) {
                            stmt.setString(C_FACTTABLECOLUMN, "");
                        }
                        if (values[C_OPERATOR_INDEX].isEmpty()) {
                            stmt.setString(C_OPERATOR, "");
                        }

                        stmt.addBatch();
                        count++;
                    } catch (Exception exception) {
                        LOGGER.error("", exception);
                    }

                    if (count == batchSize) {
                        stmt.executeBatch();
                        conn.commit();
                        stmt.clearBatch();
                        count = 0;
                    }
                }

                if (count > 0) {
                    stmt.executeBatch();
                    conn.commit();
                    stmt.clearBatch();
                }
            }
        }
    }

    protected void batchInsert(JdbcTemplate jdbcTemplate, String table, ZipEntry zipEntry, ZipFile zipFile, int batchSize) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
            // create prepared statement
            String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), getHeaders(reader.readLine()));
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());

                int count = 0;
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    // skip lines that are commented out
                    String cleanedLine = line.trim();
                    if (cleanedLine.isEmpty() || cleanedLine.startsWith("--")) {
                        continue;
                    }

                    try {
                        // add dummy value ($) at the beganing and end of the line before splitting
                        String[] temp = TAB_DELIM.split(String.format("$\t%s\t$", line));

                        // create a new array of data without the dummy values
                        String[] values = new String[temp.length - 2];
                        System.arraycopy(temp, 1, values, 0, values.length);

                        // trim each element in-place
                        Arrays.setAll(values, i -> values[i].trim());

                        setColumns(stmt, columnTypes, values);

                        // add null columns not provided
                        if (values.length < columnTypes.length) {
                            for (int i = values.length + 1; i <= columnTypes.length; i++) {
                                stmt.setNull(i, Types.NULL);
                            }
                        }

                        stmt.addBatch();
                        count++;
                    } catch (Exception exception) {
                        LOGGER.error("", exception);
                    }

                    if (count == batchSize) {
                        stmt.executeBatch();
                        conn.commit();
                        stmt.clearBatch();
                        count = 0;
                    }
                }

                if (count > 0) {
                    stmt.executeBatch();
                    conn.commit();
                    stmt.clearBatch();
                }
            }
        }
    }

    protected void insertUnique(JdbcTemplate jdbcTemplate, String table, ZipEntry zipEntry, ZipFile zipFile, String pkColumn) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
            Set<String> pkeys = getColumnData(jdbcTemplate, table, pkColumn);
            List<String> columnNames = getHeaders(reader.readLine());
            final int pkIndex = columnNames.indexOf(pkColumn);

            // create prepared statement
            String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), columnNames);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());

                int count = 0;
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    // skip lines that are commented out
                    String cleanedLine = line.trim();
                    if (cleanedLine.isEmpty() || cleanedLine.startsWith("--")) {
                        continue;
                    }

                    // add dummy value ($) at the beganing and end of the line before splitting
                    String[] temp = TAB_DELIM.split(String.format("$\t%s\t$", line));

                    // create a new array of data without the dummy values
                    String[] values = new String[temp.length - 2];
                    System.arraycopy(temp, 1, values, 0, values.length);

                    // trim each element in-place
                    Arrays.setAll(values, i -> values[i].trim());

                    if (!pkeys.contains(values[pkIndex].toLowerCase())) {
                        try {
                            setColumns(stmt, columnTypes, values);

                            // add null columns not provided
                            if (values.length < columnTypes.length) {
                                for (int i = values.length + 1; i <= columnTypes.length; i++) {
                                    stmt.setNull(i, Types.NULL);
                                }
                            }

                            stmt.addBatch();
                            count++;
                        } catch (Exception exception) {
                            LOGGER.error("", exception);
                        }
                    }

                    if (count == DEFAULT_BATCH_SIZE) {
                        stmt.executeBatch();
                        conn.commit();
                        stmt.clearBatch();
                        count = 0;
                    }
                }

                if (count > 0) {
                    stmt.executeBatch();
                    conn.commit();
                    stmt.clearBatch();
                }
            }
        }
    }

    /**
     * Get the file header (the first line of the file).
     *
     * @param header
     * @return
     * @throws IOException
     */
    protected List<String> getHeaders(String header) throws IOException {
        return (header == null || header.trim().isEmpty())
                ? Collections.EMPTY_LIST
                : Arrays.stream(TAB_DELIM.split(header))
                        .map(String::trim)
                        .filter(e -> !e.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
    }

    protected boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement pstmt = null;
                switch (conn.getMetaData().getDatabaseProductName()) {
                    case "PostgreSQL" -> {
                        pstmt = conn.prepareStatement("SELECT 1 FROM pg_tables WHERE schemaname = ? AND (tablename = UPPER(?) OR tablename = LOWER(?))");
                        pstmt.setString(1, conn.getSchema());
                        pstmt.setString(2, tableName);
                        pstmt.setString(3, tableName);
                    }
                    case "Oracle" -> {
                        pstmt = conn.prepareStatement("SELECT 1 FROM user_tables WHERE table_name = UPPER(?) OR table_name = LOWER(?)");
                        pstmt.setString(1, tableName);
                        pstmt.setString(2, tableName);
                    }
                    case "Microsoft SQL Server" -> {
                        pstmt = conn.prepareStatement("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = ? AND (table_name = UPPER(?) OR table_name = LOWER(?))");
                        pstmt.setString(1, conn.getSchema());
                        pstmt.setString(2, tableName);
                        pstmt.setString(3, tableName);
                    }
                }

                if (pstmt != null) {
                    ResultSet resultSet = pstmt.executeQuery();

                    return resultSet.next();
                }
            }
        }

        return false;
    }

    protected void createTable(JdbcTemplate jdbcTemplate, String tableName, Path file) throws SQLException, IOException {
        String query = fileSystemService.getResourceFileContents(file);

        jdbcTemplate.execute(query.replaceAll("I2B2", tableName));
    }

    protected String getDatabaseVendor(JdbcTemplate jdbcTemplate) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                return conn.getMetaData().getDatabaseProductName();
            } catch (SQLException exception) {
                LOGGER.error("", exception);
            }
        }

        return "Unknown";
    }

    protected String getDatabaseVendorName(JdbcTemplate jdbcTemplate) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                return JdbcUtils.commonDatabaseName(conn.getMetaData().getDatabaseProductName());
            } catch (SQLException exception) {
                LOGGER.error("", exception);
            }
        }

        return "Unknown";
    }

    protected void createTableIndexes(JdbcTemplate jdbcTemplate, String tableName, Path file) throws SQLException, IOException {
        List<String> queries = fileSystemService.getResourceFileContentByLines(file);
        for (String query : queries) {
            // skip lines that are commented out
            if (query.startsWith("--")) {
                continue;
            }

            query = query
                    .replaceAll(";", "")
                    .replaceAll("i2b2", tableName.toLowerCase())
                    .replaceAll("I2B2", tableName)
                    .trim();
            jdbcTemplate.execute(query);
        }
    }

    protected void setColumns(PreparedStatement stmt, int[] columnTypes, String[] values) throws SQLException, ParseException, NumberFormatException {
        for (int i = 0; i < values.length; i++) {
            int parameterIndex = i + 1;
            String value = values[i];
            if (value == null || value.isEmpty()) {
                stmt.setNull(parameterIndex, Types.NULL);
            } else {
                switch (columnTypes[i]) {
                    case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB ->
                        stmt.setString(parameterIndex, value);
                    case Types.TINYINT ->
                        stmt.setByte(parameterIndex, Byte.parseByte(value));
                    case Types.SMALLINT ->
                        stmt.setShort(parameterIndex, Short.parseShort(value));
                    case Types.INTEGER ->
                        stmt.setInt(parameterIndex, Integer.parseInt(value));
                    case Types.BIGINT ->
                        stmt.setLong(parameterIndex, Long.parseLong(value));
                    case Types.REAL, Types.FLOAT ->
                        stmt.setFloat(parameterIndex, Float.parseFloat(value));
                    case Types.DOUBLE ->
                        stmt.setDouble(parameterIndex, Double.parseDouble(value));
                    case Types.NUMERIC ->
                        stmt.setBigDecimal(parameterIndex, new BigDecimal(value));
                    case Types.DATE -> {
                        if (YYYYMMDD_PATTERN.matcher(value).matches()) {
                            stmt.setDate(parameterIndex, new Date(YYYYMMDD_DF.parse(value).getTime()));
                        } else if (YYYYMMDD_DASH_PATTERN.matcher(value).matches()) {
                            stmt.setDate(parameterIndex, new Date(YYYYMMDD_DASH_DF.parse(value).getTime()));
                        } else {
                            stmt.setDate(parameterIndex, new Date(DDMMMYY_DF.parse(value).getTime()));
                        }
                    }
                    case Types.TIME -> {
                        if (YYYYMMDD_PATTERN.matcher(value).matches()) {
                            stmt.setTime(parameterIndex, new Time(YYYYMMDD_DF.parse(value).getTime()));
                        } else if (YYYYMMDD_DASH_PATTERN.matcher(value).matches()) {
                            stmt.setTime(parameterIndex, new Time(YYYYMMDD_DASH_DF.parse(value).getTime()));
                        } else {
                            stmt.setTime(parameterIndex, new Time(DDMMMYY_DF.parse(value).getTime()));
                        }
                    }
                    case Types.TIMESTAMP -> {
                        if (YYYYMMDD_PATTERN.matcher(value).matches()) {
                            stmt.setTimestamp(parameterIndex, new Timestamp(YYYYMMDD_DF.parse(value).getTime()));
                        } else if (YYYYMMDD_DASH_PATTERN.matcher(value).matches()) {
                            stmt.setTimestamp(parameterIndex, new Timestamp(YYYYMMDD_DASH_DF.parse(value).getTime()));
                        } else {
                            stmt.setTimestamp(parameterIndex, new Timestamp(DDMMMYY_DF.parse(value).getTime()));
                        }
                    }
                    case Types.BIT ->
                        stmt.setBoolean(parameterIndex, value.equals("1"));
                    case Types.VARBINARY, Types.BINARY ->
                        stmt.setBytes(parameterIndex, value.getBytes());
                }
            }
        }
    }

    protected Set<String> getColumnData(JdbcTemplate jdbcTemplate, String table, String column) throws SQLException {
        Set<String> data = new HashSet<>();

        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                String query = String.format("SELECT %s FROM %s.%s", column, conn.getSchema(), table.toLowerCase());
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    data.add(rs.getString(1).trim().toLowerCase());
                }
            }

        }

        return data;
    }

    protected int[] getColumnTypes(ParameterMetaData metadata) throws SQLException {
        int[] types = new int[metadata.getParameterCount()];
        for (int i = 0; i < types.length; i++) {
            types[i] = metadata.getParameterType(i + 1);
        }

        return types;
    }

    protected String createInsertStatement(String schema, String tableName, List<String> columnNames) {
        String columns = columnNames.stream().collect(Collectors.joining(","));
//        String placeholder = IntStream.range(0, columnNames.size()).mapToObj(e -> "?").collect(Collectors.joining(","));
        String placeholder = String.join(",", Collections.nCopies(columnNames.size(), "?"));

        return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholder);
    }

    protected String createDeleteStatement(String schema, String tableName, String columnName) {
        return String.format("DELETE FROM %s.%s WHERE %s = ?", schema, tableName, columnName);
    }

}
