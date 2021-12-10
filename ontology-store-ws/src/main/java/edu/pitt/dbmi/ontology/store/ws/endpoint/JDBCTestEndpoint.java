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
package edu.pitt.dbmi.ontology.store.ws.endpoint;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Dec 10, 2021 10:56:00 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Path("test/jdbc")
public class JDBCTestEndpoint {

    private final JdbcTemplate ontologyDemoJdbcTemplate;
    private final JdbcTemplate ontologyACTJdbcTemplate;

    @Autowired
    public JDBCTestEndpoint(JdbcTemplate ontologyDemoJdbcTemplate, JdbcTemplate ontologyACTJdbcTemplate) {
        this.ontologyDemoJdbcTemplate = ontologyDemoJdbcTemplate;
        this.ontologyACTJdbcTemplate = ontologyACTJdbcTemplate;
    }

    @GET
    @Path("datatype/column/demo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMeta() throws Exception {
        return Response.ok(getColumnDataType(ontologyDemoJdbcTemplate)).build();
    }

    @GET
    @Path("schemes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSchemesData() throws Exception {
        String sql = String.format("SELECT * FROM %s.schemes", getMetaDataSchemaName(ontologyDemoJdbcTemplate));
        List<String> rows = ontologyDemoJdbcTemplate.query(sql, (ResultSet rs, int row) -> {
            List<String> list = new LinkedList<>();
            for (int i = 1; i <= 3; i++) {
                list.add(rs.getString(i));
            }

            return list.stream().collect(Collectors.joining(","));
        });

        return Response.ok(rows).build();
    }

    private Map<String, String> getColumnDataType(JdbcTemplate jdbcTemplate) throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();

        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, "schemes", null);
                while (columns.next()) {
                    map.put(columns.getString("COLUMN_NAME"), columns.getString("DATA_TYPE"));
                }
            }
        }

        return map;
    }

    private String getMetaDataSchemaName(JdbcTemplate jdbcTemplate) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                return conn.getSchema();
            }
        }

        return "unknown";
    }

}
