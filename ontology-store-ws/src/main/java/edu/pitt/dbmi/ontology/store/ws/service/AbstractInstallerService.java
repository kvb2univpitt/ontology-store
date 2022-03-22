/*
 * Copyright (C) 2022 University of Pittsburgh.
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
package edu.pitt.dbmi.ontology.store.ws.service;

import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Feb 24, 2022 2:44:08 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public abstract class AbstractInstallerService {

    protected static final String ACTION_TYPE = "Install";

    protected static final Pattern TAB_DELIM = Pattern.compile("\t");
    protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yy");

    protected static final int DEFAULT_BATCH_SIZE = 25000;

    protected final FileSysService fileSysService;

    public AbstractInstallerService(FileSysService fileSysService) {
        this.fileSysService = fileSysService;
    }

    public abstract ActionSummary install(JdbcTemplate jdbcTemplate, OntologyProductAction action);

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
                exception.printStackTrace(System.err);
            }
        }

        return "Unknown";
    }

    protected void createTableIndexes(JdbcTemplate jdbcTemplate, String tableName, Path file) throws SQLException, IOException {
        List<String> queries = fileSysService.getResourceFileContentByLines(file);
        for (String query : queries) {
            query = query
                    .replaceAll(";", "")
                    .replaceAll("i2b2", tableName.toLowerCase())
                    .replaceAll("I2B2", tableName)
                    .trim();
            jdbcTemplate.execute(query);
        }
    }

    protected void insert(JdbcTemplate jdbcTemplate, String table, Path file) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement
                String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), fileSysService.getHeaders(file));
                PreparedStatement stmt = conn.prepareStatement(sql);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());
                Files.lines(file)
                        .skip(1)
                        .filter(line -> !line.trim().isEmpty())
                        .map(TAB_DELIM::split)
                        .forEach(values -> {
                            try {
                                setColumns(stmt, columnTypes, values);

                                // add null columns not provided
                                if (values.length < columnTypes.length) {
                                    for (int i = values.length; i < columnTypes.length; i++) {
                                        stmt.setNull(i + 1, Types.NULL);
                                    }
                                }

                                stmt.execute();
                            } catch (Exception exception) {
                            }
                        });
            }
        }
    }

    protected void batchInsert(JdbcTemplate jdbcTemplate, String table, Path file, int batchSize) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement
                String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), fileSysService.getHeaders(file));
                PreparedStatement stmt = conn.prepareStatement(sql);

                // get columnTypes
                int count = 0;
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    // skip header
                    reader.readLine();

                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
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
                            exception.printStackTrace(System.err);
                        }

                        stmt.addBatch();
                        count++;
                        if (count == batchSize) {
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
    }

    protected void insertUnique(JdbcTemplate jdbcTemplate, String table, Path file, String pkColumn) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            Set<String> pkeys = getColumnData(jdbcTemplate, table, pkColumn);
            List<String> columnNames = fileSysService.getHeaders(file);
            final int pkIndex = columnNames.indexOf(pkColumn);

            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement
                String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), columnNames);
                PreparedStatement stmt = conn.prepareStatement(sql);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());
                Files.lines(file)
                        .skip(1)
                        .filter(line -> !line.trim().isEmpty())
                        .map(TAB_DELIM::split)
                        .forEach(values -> {
                            if (!pkeys.contains(values[pkIndex].toLowerCase())) {
                                try {
                                    for (int i = 0; i < values.length; i++) {
                                        int colIndex = i + 1;
                                        String value = values[i].trim();
                                        if (value.isEmpty()) {
                                            stmt.setNull(colIndex, Types.NULL);
                                        } else {
                                            setColumns(stmt, columnTypes, values);
                                        }
                                    }

                                    // add null columns not provided
                                    if (values.length < columnTypes.length) {
                                        for (int i = values.length; i < columnTypes.length; i++) {
                                            stmt.setNull(i + 1, Types.NULL);
                                        }
                                    }

                                    stmt.execute();
                                } catch (Exception exception) {
                                }
                            }
                        });
            }
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

}
