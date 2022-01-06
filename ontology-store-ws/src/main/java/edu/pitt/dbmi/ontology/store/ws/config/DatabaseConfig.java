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
package edu.pitt.dbmi.ontology.store.ws.config;

import edu.pitt.dbmi.ontology.store.ws.db.OntologyDBAccess;
import edu.pitt.dbmi.ontology.store.ws.db.PostgreSQLOntologyDBAccess;
import edu.pitt.dbmi.ontology.store.ws.service.FileSysService;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Dec 8, 2021 12:44:59 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public OntologyDBAccess ontologyDBAccess(JdbcTemplate jdbcTemplate, FileSysService fileSysService) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metadata = conn.getMetaData();
                switch (metadata.getDatabaseProductName()) {
                    case "PostgreSQL":
                        return new PostgreSQLOntologyDBAccess(jdbcTemplate, fileSysService);
                }
            } catch (SQLException exception) {
                exception.printStackTrace(System.err);
            }
        }

        return null;
    }

}
