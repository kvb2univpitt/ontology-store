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

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @ConfigurationProperties(prefix = "spring.datasource.ontologydemods")
    public JNDIName ontologyDemoJNDIName() {
        return new JNDIName();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ontologyactds")
    public JNDIName ontologyACTJNDIName() {
        return new JNDIName();
    }

    @Bean
    public DataSource ontologyDemoDataSource() {
        return (new JndiDataSourceLookup()).getDataSource(ontologyDemoJNDIName().getJndiName());
    }

    @Bean
    public DataSource ontologyACTDataSource() {
        return (new JndiDataSourceLookup()).getDataSource(ontologyACTJNDIName().getJndiName());
    }

    @Bean
    public JdbcTemplate ontologyACTJdbcTemplate() {
        return new JdbcTemplate(ontologyACTDataSource());
    }

    @Bean
    public JdbcTemplate ontologyDemoJdbcTemplate() {
        return new JdbcTemplate(ontologyDemoDataSource());
    }

    public class JNDIName {

        private String jndiName;

        public JNDIName() {
        }

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }

    }

}
