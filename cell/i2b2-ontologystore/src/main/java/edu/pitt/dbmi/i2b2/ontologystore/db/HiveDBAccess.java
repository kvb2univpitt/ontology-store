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
package edu.pitt.dbmi.i2b2.ontologystore.db;

import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Oct 14, 2022 12:13:07 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class HiveDBAccess {

    private static final Log LOGGER = LogFactory.getLog(HiveDBAccess.class);

    private final String QUERY_ONT_DATASOURCE = "SELECT c_db_datasource FROM ont_db_lookup WHERE c_project_path = ?";
    private final String QUERY_CRC_DATASOURCE = "SELECT c_db_datasource FROM crc_db_lookup WHERE c_project_path = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HiveDBAccess(DataSource hiveDataSource) {
        this.jdbcTemplate = new JdbcTemplate(hiveDataSource);
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
