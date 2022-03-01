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
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Mar 1, 2022 2:59:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CrcInstallerService extends AbstractInstallerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstallerService.class);

    private static final String CONCEPT_DIMENSION_TABLE = "concept_dimension";

    private static final String QT_BREAKDOWN_PATH_TABLE = "qt_breakdown_path";
    protected static final String QT_BREAKDOWN_PATH_TABLE_PK = "name";

    @Autowired
    public CrcInstallerService(FileSysService fileSysService) {
        super(fileSysService);
    }

    @Override
    public void install(JdbcTemplate jdbcTemplate, List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        actions.stream().filter(e -> e.isInstall()).forEach(action -> summaries.add(install(jdbcTemplate, action)));
    }

    private ActionSummary install(JdbcTemplate jdbcTemplate, OntologyProductAction action) {
        String productFolder = action.getKey().replaceAll(".json", "");

        // import crc data
        for (Path crcData : fileSysService.getCrcData(productFolder)) {
            try {
                insertIntoConceptDimensionTable(jdbcTemplate, crcData);
            } catch (SQLException | IOException exception) {
                String errMsg = String.format("Failed to import ontology from file '%s'.", crcData.toString());
                LOGGER.error(errMsg, exception);

                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "CRC Data Installation Failed.");
            }
        }

        // import qt-breakdown-path data
        try {
            insertIntoQtBreakdownPathTable(jdbcTemplate, fileSysService.getQtBreakdownPathFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("QT_BREAKDOWN_PATH.tsv insertion error.", exception);
            fileSysService.createInstallFailedIndicatorFile(productFolder);

            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "CRC Installation Failed.");
        }

        fileSysService.createInstallFinishedIndicatorFile(productFolder);

        return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "CRC Data Installed.");
    }

    private void insertIntoQtBreakdownPathTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        insertUnique(jdbcTemplate, QT_BREAKDOWN_PATH_TABLE, file, QT_BREAKDOWN_PATH_TABLE_PK);
    }

    private void insertIntoConceptDimensionTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        batchInsert(jdbcTemplate, CONCEPT_DIMENSION_TABLE, file, 5000);
    }

}
