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
package edu.pitt.dbmi.ontology.store.ws.service;

import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 24, 2022 2:43:39 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class OntInstallerService extends AbstractInstallerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstallerService.class);

    protected static final String SCHEMES_TABLE_NAME = "schemes";
    protected static final String SCHEMES_TABLE_PK = "c_key";

    protected static final String TABLE_ACCESS_TABLE_NAME = "table_access";
    protected static final String TABLE_ACCESS_TABLE_PK = "c_table_cd";

    @Autowired
    public OntInstallerService(FileSysService fileSysService) {
        super(fileSysService);
    }

    @Override
    public void install(JdbcTemplate jdbcTemplate, List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        actions.stream().filter(e -> e.isInstall()).forEach(action -> summaries.add(install(jdbcTemplate, action)));
    }

    private ActionSummary install(JdbcTemplate jdbcTemplate, OntologyProductAction action) {
        String productFolder = action.getKey().replaceAll(".json", "");

        // import ontology
        for (Path ontology : fileSysService.getOntologies(productFolder)) {
            String fileName = ontology.getFileName().toString();
            String tableName = fileName.replaceAll(".tsv", "");

            try {
                createOntologyTable(jdbcTemplate, tableName);
                insertIntoOntologyTable(jdbcTemplate, tableName, ontology);
                createOntologyTableIndices(jdbcTemplate, tableName);
            } catch (SQLException | IOException exception) {
                String errMsg = String.format("Failed to import ontology from file '%s'.", ontology.toString());
                LOGGER.error(errMsg, exception);

                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
            }
        }

        // import schemes data
        try {
            insertIntoSchemesTable(jdbcTemplate, fileSysService.getSchemesFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("SCHEMES.tsv insertion error.", exception);
            fileSysService.createInstallFailedIndicatorFile(productFolder);

            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
        }

        // import table access data
        try {
            insertIntoTableAccessTable(jdbcTemplate, fileSysService.getTableAccessFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("TABLE_ACCESS.tsv insertion error.", exception);
            fileSysService.createInstallFailedIndicatorFile(productFolder);

            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
        }

        fileSysService.createInstallFinishedIndicatorFile(productFolder);

        return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Installed.");
    }

    private void insertIntoTableAccessTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        insertUnique(jdbcTemplate, TABLE_ACCESS_TABLE_NAME, file, TABLE_ACCESS_TABLE_PK);
    }

    private void insertIntoSchemesTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        insertUnique(jdbcTemplate, SCHEMES_TABLE_NAME, file, SCHEMES_TABLE_PK);
    }

    private void createOntologyTableIndices(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        createTableIndexes(jdbcTemplate, tableName, Paths.get("ont", "ontology_table_indices.sql"));
    }

    private void insertIntoOntologyTable(JdbcTemplate jdbcTemplate, String tableName, Path file) throws SQLException, IOException {
        batchInsert(jdbcTemplate, tableName, file, 5000);
    }

    private void createOntologyTable(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        switch (getDatabaseVendor(jdbcTemplate)) {
            case "PostgreSQL":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "postgresql", "ontology_table.sql"));
                break;
            case "Oracle":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "oracle", "ontology_table.sql"));
                break;
            case "Microsoft SQL Server":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "sqlserver", "ontology_table.sql"));
                break;
        }
    }

}
