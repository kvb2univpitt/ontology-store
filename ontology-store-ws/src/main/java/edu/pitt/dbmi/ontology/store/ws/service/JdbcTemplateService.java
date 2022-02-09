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
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 10, 2021 11:42:56 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class JdbcTemplateService {

    public JdbcTemplateService() {
    }

    /**
     * Return metadata schema name.
     *
     * @param jdbcTemplate
     * @return
     * @throws SQLException
     */
    public String getSchema(JdbcTemplate jdbcTemplate) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                return conn.getSchema();
            }
        }

        return "";
    }

}
