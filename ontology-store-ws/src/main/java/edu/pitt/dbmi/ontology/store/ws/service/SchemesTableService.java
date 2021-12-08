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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
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

    private final JdbcTemplate ontologyDemoJdbcTemplate;

    @Autowired
    public SchemesTableService(JdbcTemplate ontologyDemoJdbcTemplate) {
        this.ontologyDemoJdbcTemplate = ontologyDemoJdbcTemplate;
    }

    public List<String> getAll() throws SQLException {
        String sql = String.format("SELECT * FROM %s.schemes", getMetaDataSchemaName());
        List<String> rows = ontologyDemoJdbcTemplate.query(sql, (ResultSet rs, int row) -> {
            List<String> list = new LinkedList<>();
            for (int i = 1; i <= 3; i++) {
                list.add(rs.getString(i));
            }

            return list.stream().collect(Collectors.joining(","));
        });

        return rows;
    }

    /**
     * Return metadata schema name.
     *
     * @return
     * @throws SQLException
     */
    public String getMetaDataSchemaName() throws SQLException {
        try (Connection conn = ontologyDemoJdbcTemplate.getDataSource().getConnection()) {
            return conn.getSchema();
        }
    }

}
