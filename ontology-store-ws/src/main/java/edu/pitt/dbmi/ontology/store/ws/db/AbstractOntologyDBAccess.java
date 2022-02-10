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
package edu.pitt.dbmi.ontology.store.ws.db;

import edu.pitt.dbmi.ontology.store.ws.service.FileSysService;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Jan 6, 2022 12:50:23 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractOntologyDBAccess {

    protected static final String SCHEMES_TABLE_NAME = "schemes";
    protected static final String TABLE_ACCESS_TABLE_NAME = "table_access";

    protected static final Pattern TAB_DELIM = Pattern.compile("\t");
    protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yy");

    private final Path ontologyTableIndicesFile = Paths.get("sql", "ontology_table_indices.sql");

    protected final JdbcTemplate jdbcTemplate;
    protected final FileSysService fileSysService;

    public AbstractOntologyDBAccess(JdbcTemplate jdbcTemplate, FileSysService fileSysService) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileSysService = fileSysService;
    }

    protected void createOntologyTable(Path file, String tableName) throws SQLException, IOException {
        String sql = fileSysService.getResourceFileContents(file);

        jdbcTemplate.execute(sql.replaceAll("I2B2", tableName));
    }

    protected void createIndicesForOntologyTable(String tableName) throws SQLException, IOException {
        String sql = fileSysService.getResourceFileContents(ontologyTableIndicesFile);
        sql = sql.replaceAll("i2b2", tableName.toLowerCase());
        sql = sql.replaceAll("I2B2", tableName);

        jdbcTemplate.execute(sql);
    }

    protected void insert(Path file, String table) throws SQLException, IOException {
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
                        .forEach(fields -> {
                            try {
                                for (int i = 0; i < fields.length; i++) {
                                    int colIndex = i + 1;
                                    String value = fields[i].trim();
                                    if (value.isEmpty()) {
                                        stmt.setNull(colIndex, Types.NULL);
                                    } else {
                                        switch (columnTypes[i]) {
                                            case Types.CHAR:
                                            case Types.VARCHAR:
                                            case Types.LONGVARCHAR:
                                            case Types.CLOB:
                                                stmt.setString(colIndex, value);
                                                break;
                                            case Types.TINYINT:
                                                stmt.setByte(colIndex, Byte.parseByte(value));
                                                break;
                                            case Types.SMALLINT:
                                                stmt.setShort(colIndex, Short.parseShort(value));
                                                break;
                                            case Types.INTEGER:
                                                stmt.setInt(colIndex, Integer.parseInt(value));
                                                break;
                                            case Types.BIGINT:
                                                stmt.setLong(colIndex, Long.parseLong(value));
                                                break;
                                            case Types.REAL:
                                            case Types.FLOAT:
                                                stmt.setFloat(colIndex, Float.parseFloat(value));
                                                break;
                                            case Types.DOUBLE:
                                                stmt.setDouble(colIndex, Double.parseDouble(value));
                                                break;
                                            case Types.NUMERIC:
                                                stmt.setBigDecimal(colIndex, new BigDecimal(value));
                                                break;
                                            case Types.DATE:
                                                stmt.setDate(colIndex, new Date(DATE_FORMATTER.parse(value).getTime()));
                                                break;
                                            case Types.TIME:
                                                stmt.setTime(colIndex, new Time(DATE_FORMATTER.parse(value).getTime()));
                                                break;
                                            case Types.TIMESTAMP:
                                                stmt.setTimestamp(colIndex, new Timestamp(DATE_FORMATTER.parse(value).getTime()));
                                                break;
                                            case Types.BIT:
                                                stmt.setBoolean(colIndex, value.equals("1"));
                                                break;
                                            case Types.VARBINARY:
                                            case Types.BINARY:
                                                stmt.setBytes(colIndex, value.getBytes());
                                                break;
                                        }
                                    }
                                }

                                // add null columns not provided
                                if (fields.length < columnTypes.length) {
                                    for (int i = fields.length; i < columnTypes.length; i++) {
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

    protected void batchInsert(Path file, String table, int batchSize) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement
                String sql = createInsertStatement(conn.getSchema(), table.toLowerCase(), fileSysService.getHeaders(file));
                PreparedStatement stmt = conn.prepareStatement(sql);

                // get columnTypes
                int[] columnTypes = getColumnTypes(stmt.getParameterMetaData());
                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    int count = 0;
                    int lineNum = 0;
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        lineNum++;
                        if (lineNum == 1) {
                            continue;
                        }

                        try {
                            String[] fields = TAB_DELIM.split(line);
                            for (int i = 0; i < fields.length; i++) {
                                int colIndex = i + 1;
                                String value = fields[i].trim();
                                if (value.isEmpty()) {
                                    stmt.setNull(colIndex, Types.NULL);
                                } else {
                                    switch (columnTypes[i]) {
                                        case Types.CHAR:
                                        case Types.VARCHAR:
                                        case Types.LONGVARCHAR:
                                        case Types.CLOB:
                                            stmt.setString(colIndex, value);
                                            break;
                                        case Types.TINYINT:
                                            stmt.setByte(colIndex, Byte.parseByte(value));
                                            break;
                                        case Types.SMALLINT:
                                            stmt.setShort(colIndex, Short.parseShort(value));
                                            break;
                                        case Types.INTEGER:
                                            stmt.setInt(colIndex, Integer.parseInt(value));
                                            break;
                                        case Types.BIGINT:
                                            stmt.setLong(colIndex, Long.parseLong(value));
                                            break;
                                        case Types.REAL:
                                        case Types.FLOAT:
                                            stmt.setFloat(colIndex, Float.parseFloat(value));
                                            break;
                                        case Types.DOUBLE:
                                            stmt.setDouble(colIndex, Double.parseDouble(value));
                                            break;
                                        case Types.NUMERIC:
                                            stmt.setBigDecimal(colIndex, new BigDecimal(value));
                                            break;
                                        case Types.DATE:
                                            stmt.setDate(colIndex, new Date(DATE_FORMATTER.parse(value).getTime()));
                                            break;
                                        case Types.TIME:
                                            stmt.setTime(colIndex, new Time(DATE_FORMATTER.parse(value).getTime()));
                                            break;
                                        case Types.TIMESTAMP:
                                            stmt.setTimestamp(colIndex, new Timestamp(DATE_FORMATTER.parse(value).getTime()));
                                            break;
                                        case Types.BIT:
                                            stmt.setBoolean(colIndex, value.equals("1"));
                                            break;
                                        case Types.VARBINARY:
                                        case Types.BINARY:
                                            stmt.setBytes(colIndex, value.getBytes());
                                            break;
                                    }
                                }
                            }

                            // add null columns not provided
                            if (fields.length < columnTypes.length) {
                                for (int i = fields.length; i < columnTypes.length; i++) {
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
                stmt.executeBatch();
            }
        }
    }

    private int[] getColumnTypes(ParameterMetaData metadata) throws SQLException {
        int[] types = new int[metadata.getParameterCount()];
        for (int i = 0; i < types.length; i++) {
            types[i] = metadata.getParameterType(i + 1);
        }

        return types;
    }

    private String createInsertStatement(String schema, String tableName, List<String> columnNames) {
        String columns = columnNames.stream().collect(Collectors.joining(","));
        String placeholder = IntStream.range(0, columnNames.size()).mapToObj(e -> "?").collect(Collectors.joining(","));

        return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholder);
    }

}
