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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 8, 2021 2:02:01 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class SchemesTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemesTableService.class);

    private static final Pattern TAB_DELIM = Pattern.compile("\t");

    private static final String INSERT_QUERY = "INSERT INTO %s.schemes (C_KEY,C_NAME,C_DESCRIPTION) VALUES (?,?,?)";

    private final JdbcTemplate ontologyDemoJdbcTemplate;

    @Autowired
    public SchemesTableService(JdbcTemplate ontologyDemoJdbcTemplate) {
        this.ontologyDemoJdbcTemplate = ontologyDemoJdbcTemplate;
    }

    public void insert(Path file) throws SQLException {
        DataSource dataSource = ontologyDemoJdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                String sql = String.format(INSERT_QUERY, conn.getSchema());
                PreparedStatement stmt = conn.prepareStatement(sql);
                try {
                    Files.lines(file)
                            .skip(1)
                            .map(String::trim)
                            .filter(line -> !line.isEmpty())
                            .map(TAB_DELIM::split)
                            .filter(fields -> fields.length == 3)
                            .forEach(fields -> {
                                try {
                                    stmt.setString(1, fields[0]);
                                    stmt.setString(2, fields[1]);
                                    stmt.setString(3, fields[2]);

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

}
