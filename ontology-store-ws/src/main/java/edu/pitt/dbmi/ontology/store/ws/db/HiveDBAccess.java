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

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Feb 23, 2022 4:29:24 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class HiveDBAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveDBAccess.class);

    private final String QUERY_ONT_DATASOURCE = "SELECT c_db_datasource FROM ont_db_lookup WHERE c_project_path = ?";
    private final String QUERY_CRC_DATASOURCE = "SELECT c_db_datasource FROM crc_db_lookup WHERE c_project_path = ?";

    private final JdbcTemplate jdbcTemplate;

    public HiveDBAccess(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String getOntDataSourceJNDIName(String project) {
        String projectPath = String.format("%s/", project);
        try {
            return jdbcTemplate.queryForObject(QUERY_ONT_DATASOURCE, String.class, new Object[]{projectPath});
        } catch (Exception exception) {
            String errMsg = String.format("Unable to get ONT JNDI name for project %s.", project);
            LOGGER.error(errMsg, exception);
            return null;
        }
    }

    public String getCrcDataSourceJNDIName(String project) {
        String projectPath = String.format("/%s/", project);
        try {
            return jdbcTemplate.queryForObject(QUERY_CRC_DATASOURCE, String.class, new Object[]{projectPath});
        } catch (Exception exception) {
            String errMsg = String.format("Unable to get CRC JNDI name for project %s.", project);
            LOGGER.error(errMsg, exception);
            return null;
        }
    }

}
