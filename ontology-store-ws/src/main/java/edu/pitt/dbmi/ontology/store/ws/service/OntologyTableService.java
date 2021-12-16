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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 15, 2021 10:49:28 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyTableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyTableService.class);

    private final JdbcTemplate ontologyDemoJdbcTemplate;
    private final JdbcTemplateService jdbcTemplateService;
    private final TableInsertService tableInsertService;
    private final ResourcePatternResolver resourcePatternResolver;

    @Autowired
    public OntologyTableService(JdbcTemplate ontologyDemoJdbcTemplate, JdbcTemplateService jdbcTemplateService, TableInsertService tableInsertService, ResourcePatternResolver resourcePatternResolver) {
        this.ontologyDemoJdbcTemplate = ontologyDemoJdbcTemplate;
        this.jdbcTemplateService = jdbcTemplateService;
        this.tableInsertService = tableInsertService;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    private String loadCreateTableQuery(ResourcePatternResolver resolver) throws IOException {
        List<String> list = new LinkedList<>();

        Resource resource = resolver.getResource("classpath:/sql/create_postgresql_ontology_table.sql");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }
        }

        return list.stream()
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.joining());
    }

    public void install(Path ontologyDir) throws SQLException, IOException {
        List<Path> files = Files.list(ontologyDir)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            String tableName = fileName.replaceAll(".tsv", "");

            createTable(tableName);

            tableInsertService.insert(file, tableName, ontologyDemoJdbcTemplate);
        }
    }

    private void createTable(String tableName) throws SQLException, IOException {
        String createTableQuery = loadCreateTableQuery(resourcePatternResolver);

        String schema = jdbcTemplateService.getSchema(ontologyDemoJdbcTemplate);
        String table = schema.isEmpty() ? tableName : String.format("%s.%s", schema, tableName);

        String sqlCreateTable = createTableQuery.replace("I2B2", table);
        String sqlMetadataPermission = "GRANT TRUNCATE,DELETE,UPDATE,REFERENCES,SELECT,INSERT,TRIGGER ON TABLE " + table + " TO i2b2metadata";
        String sqlDemodataPermission = "GRANT TRUNCATE,DELETE,UPDATE,REFERENCES,SELECT,INSERT,TRIGGER ON TABLE " + table + " TO i2b2demodata";

        ontologyDemoJdbcTemplate.execute(sqlCreateTable);
        ontologyDemoJdbcTemplate.execute(sqlMetadataPermission);
        ontologyDemoJdbcTemplate.execute(sqlDemodataPermission);
    }

}
