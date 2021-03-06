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

import edu.pitt.dbmi.ontology.store.ws.db.HiveDBAccess;
import edu.pitt.dbmi.ontology.store.ws.db.PmDBAccess;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 *
 * Dec 8, 2021 12:44:59 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource hiveDataSource(@Value("${spring.hive.datasource.jndi-name}") String datasourceJNDIName) {
        return (new JndiDataSourceLookup()).getDataSource(datasourceJNDIName);
    }

    @Bean
    public DataSource pmDataSource(@Value("${spring.pm.datasource.jndi-name}") String datasourceJNDIName) {
        return (new JndiDataSourceLookup()).getDataSource(datasourceJNDIName);
    }

    @Bean
    public HiveDBAccess hiveDBAccess(DataSource hiveDataSource) {
        return new HiveDBAccess(hiveDataSource);
    }

    @Bean
    public PmDBAccess pmDBAccess(DataSource pmDataSource) {
        return new PmDBAccess(pmDataSource);
    }

}
