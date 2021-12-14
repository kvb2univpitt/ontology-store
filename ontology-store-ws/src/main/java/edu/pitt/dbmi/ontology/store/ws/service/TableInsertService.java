/*
 * Copyright (C) 2021 University of Pittsburgh.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 14, 2021 3:22:00 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class TableInsertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableInsertService.class);

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yy");

    private final FileSysService fileSysService;

    @Autowired
    public TableInsertService(FileSysService fileSysService) {
        this.fileSysService = fileSysService;
    }

    public void insert(Path file, String tableName, JdbcTemplate jdbcTemplate) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                try {
                    List<String> columnNames = fileSysService.getHeaders(file);
                    String sql = createInsertStatement(conn.getSchema(), tableName, columnNames);
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    ParameterMetaData metadata = stmt.getParameterMetaData();
                    Files.lines(file)
                            .skip(1)
                            .forEach(line -> {
                                String[] fields = FileSysService.TAB_DELIM.split(line);
                                LOGGER.info(String.format("Inserting: %s", line));
                                try {
                                    // set parameters
                                    for (int i = 0; i < fields.length; i++) {
                                        int colIndex = i + 1;
                                        String value = fields[i];
                                        if (value == null) {
                                            stmt.setNull(colIndex, Types.NULL);
                                        } else {
                                            value = value.trim();
                                            if (value.isEmpty()) {
                                                stmt.setNull(colIndex, Types.NULL);
                                            } else {
                                                switch (metadata.getParameterClassName(colIndex)) {
                                                    case "java.lang.String":
                                                        stmt.setString(colIndex, value);
                                                        break;
                                                    case "java.lang.Integer":
                                                        stmt.setInt(colIndex, Integer.parseInt(value));
                                                        break;
                                                    case "java.sql.Timestamp":
                                                        stmt.setTimestamp(colIndex, new Timestamp(DATE_FORMATTER.parse(value).getTime()));
                                                        break;
                                                };
                                            }
                                        }
                                    }

                                    for (int i = fields.length; i < columnNames.size(); i++) {
                                        stmt.setNull(i + 1, Types.NULL);
                                    }

                                    // run query
                                    stmt.execute();
                                } catch (Exception exception) {
                                    LOGGER.error("", exception);
                                }
                            });
                } catch (IOException exception) {
                    LOGGER.error("", exception);
                }
            }
        }
    }

    public String createInsertStatement(String schema, String tableName, List<String> columnNames) {
        String columns = columnNames.stream().collect(Collectors.joining(","));
        String placeholder = IntStream.range(0, columnNames.size()).mapToObj(e -> "?").collect(Collectors.joining(","));

        return String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, tableName, columns, placeholder);
    }

}
