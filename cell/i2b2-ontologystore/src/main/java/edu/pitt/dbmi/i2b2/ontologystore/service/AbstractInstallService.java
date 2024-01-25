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
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Jan 19, 2024 9:36:38 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractInstallService {

    private static final Log LOGGER = LogFactory.getLog(AbstractInstallService.class);

    protected static final String ACTION_TYPE = "Install";

    protected static final Pattern TAB_DELIM = Pattern.compile("\t");
    protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yy");

    protected static final int DEFAULT_BATCH_SIZE = 10000;

    protected final FileSysService fileSysService;

    public AbstractInstallService(FileSysService fileSysService) {
        this.fileSysService = fileSysService;
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

    protected void batchInsert(JdbcTemplate jdbcTemplate, String table, ZipEntry zipEntry, ZipFile zipFile, int batchSize) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
            // create prepared statement
            String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), getHeaders(reader.readLine()));
            PreparedStatement stmt = conn.prepareStatement(sql);

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
                    String[] values = TAB_DELIM.split(line);

                    setColumns(stmt, columnTypes, values);

                    // add null columns not provided
                    if (values.length < columnTypes.length) {
                        for (int i = values.length; i < columnTypes.length; i++) {
                            stmt.setNull(i + 1, Types.NULL);
                        }
                    }
                } catch (Exception exception) {
                    LOGGER.error("", exception);
                }

                stmt.addBatch();
                count++;
                if (count == batchSize) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                    count = 0;
                }
            }

            if (count > 0) {
                stmt.executeBatch();
                stmt.clearBatch();
                count = 0;
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
            PreparedStatement stmt = conn.prepareStatement(sql);

            // get columnTypes
            int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());

            int count = 0;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                // skip lines that are commented out
                String cleanedLine = line.trim();
                if (cleanedLine.isEmpty() || cleanedLine.startsWith("--")) {
                    continue;
                }

                String[] values = TAB_DELIM.split(line);
                if (!pkeys.contains(values[pkIndex].toLowerCase())) {
                    try {
                        setColumns(stmt, columnTypes, values);

                        // add null columns not provided
                        if (values.length < columnTypes.length) {
                            for (int i = values.length; i < columnTypes.length; i++) {
                                stmt.setNull(i + 1, Types.NULL);
                            }
                        }
                    } catch (Exception exception) {
                        LOGGER.error("", exception);
                    }

                    stmt.addBatch();
                    count++;
                    if (count == DEFAULT_BATCH_SIZE) {
                        stmt.executeBatch();
                        stmt.clearBatch();
                        count = 0;
                    }
                }
            }

            if (count > 0) {
                stmt.executeBatch();
                stmt.clearBatch();
                count = 0;
            }
        }
    }

    /**
     * Get the file header (the first line of the file).
     *
     * @param file
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
                    case "PostgreSQL":
                        pstmt = conn.prepareStatement("SELECT 1 FROM pg_tables WHERE schemaname = ? AND (tablename = UPPER(?) OR tablename = LOWER(?))");
                        pstmt.setString(1, conn.getSchema());
                        pstmt.setString(2, tableName);
                        pstmt.setString(3, tableName);
                        break;
                    case "Oracle":
                        pstmt = conn.prepareStatement("SELECT 1 FROM user_tables WHERE table_name = UPPER(?) OR table_name = LOWER(?)");
                        pstmt.setString(1, tableName);
                        pstmt.setString(2, tableName);
                        break;
                    case "Microsoft SQL Server":
                        pstmt = conn.prepareStatement("SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = ? AND (table_name = UPPER(?) OR table_name = LOWER(?))");
                        pstmt.setString(1, conn.getSchema());
                        pstmt.setString(2, tableName);
                        pstmt.setString(3, tableName);
                        break;
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
        String query = fileSysService.getResourceFileContents(file);

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

    protected void createTableIndexes(JdbcTemplate jdbcTemplate, String tableName, Path file) throws SQLException, IOException {
        List<String> queries = fileSysService.getResourceFileContentByLines(file);
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
            int columnIndex = i + 1;
            String value = values[i].trim();
            if (value.isEmpty()) {
                stmt.setNull(columnIndex, Types.NULL);
            } else {
                switch (columnTypes[i]) {
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.CLOB:
                        stmt.setString(columnIndex, value);
                        break;
                    case Types.TINYINT:
                        stmt.setByte(columnIndex, Byte.parseByte(value));
                        break;
                    case Types.SMALLINT:
                        stmt.setShort(columnIndex, Short.parseShort(value));
                        break;
                    case Types.INTEGER:
                        stmt.setInt(columnIndex, Integer.parseInt(value));
                        break;
                    case Types.BIGINT:
                        stmt.setLong(columnIndex, Long.parseLong(value));
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        stmt.setFloat(columnIndex, Float.parseFloat(value));
                        break;
                    case Types.DOUBLE:
                        stmt.setDouble(columnIndex, Double.parseDouble(value));
                        break;
                    case Types.NUMERIC:
                        stmt.setBigDecimal(columnIndex, new BigDecimal(value));
                        break;
                    case Types.DATE:
                        stmt.setDate(columnIndex, new Date(DATE_FORMATTER.parse(value).getTime()));
                        break;
                    case Types.TIME:
                        stmt.setTime(columnIndex, new Time(DATE_FORMATTER.parse(value).getTime()));
                        break;
                    case Types.TIMESTAMP:
                        stmt.setTimestamp(columnIndex, new Timestamp(DATE_FORMATTER.parse(value).getTime()));
                        break;
                    case Types.BIT:
                        stmt.setBoolean(columnIndex, value.equals("1"));
                        break;
                    case Types.VARBINARY:
                    case Types.BINARY:
                        stmt.setBytes(columnIndex, value.getBytes());
                        break;
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
        String placeholder = IntStream.range(0, columnNames.size()).mapToObj(e -> "?").collect(Collectors.joining(","));

        return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholder);
    }

    protected String createDeleteStatement(String schema, String tableName, String columnName) {
        return String.format("DELETE FROM  %s.%s WHERE %s = ?", schema, tableName, columnName);
    }

}
