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

import edu.pitt.dbmi.ontology.store.ws.db.OntologyDBAccess;
import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 9, 2021 10:47:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyInstallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final FileSysService fileSysService;
    private final OntologyDBAccess ontologyDBAccess;

    @Autowired
    public OntologyInstallService(FileSysService fileSysService, OntologyDBAccess ontologyDBAccess) {
        this.fileSysService = fileSysService;
        this.ontologyDBAccess = ontologyDBAccess;
    }

    public synchronized void performInstallation(List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        actions.stream()
                .filter(e -> e.isInstall())
                .forEach(action -> summaries.add(install(action)));
    }

    private ActionSummary install(OntologyProductAction action) {
        String productFolder = action.getKey().replaceAll(".json", "");

        // validation
        if (fileSysService.hasFinshedDownload(productFolder)) {
            if (fileSysService.hasFinshedInstall(productFolder)) {
                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Already Installed.");
            } else if (fileSysService.hasFailedInstall(productFolder)) {
                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation previously failed.");
            } else if (fileSysService.hasStartedInstall(productFolder)) {
                return new ActionSummary(action.getTitle(), ACTION_TYPE, true, false, "Installation already started.");
            }
        } else if (fileSysService.hasFailedDownload(productFolder)) {
            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download previously failed.");
        } else if (fileSysService.hasStartedDownload(productFolder)) {
            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download not finished.");
        } else {
            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Has not been downloaded.");
        }

        fileSysService.createInstallStartedIndicatorFile(productFolder);

        List<Path> ontologies = fileSysService.getOntologies(productFolder);
        for (Path ontology : ontologies) {
            String fileName = ontology.getFileName().toString();
            String tableName = fileName.replaceAll(".tsv", "");

            try {
                ontologyDBAccess.createOntologyTable(tableName);
                ontologyDBAccess.insertIntoOntologyTable(ontology, tableName);
                ontologyDBAccess.createOntologyTableIndices(tableName);
            } catch (SQLException | IOException exception) {
                LOGGER.error(
                        String.format("Failed to import ontology from file '%s'.", ontology.toString()),
                        exception);
                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
            }

            try {
                ontologyDBAccess.insertIntoSchemesTable(fileSysService.getSchemesFile(productFolder));
            } catch (SQLException | IOException exception) {
                LOGGER.error("SCHEMES.tsv insertion error.", exception);
                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
            }

            try {
                ontologyDBAccess.insertIntoTableAccessTable(fileSysService.getTableAccessFile(productFolder));
            } catch (SQLException | IOException exception) {
                LOGGER.error("TABLE_ACCESS.tsv insertion error.", exception);
                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation Failed.");
            }
        }

        fileSysService.createInstallFinishedIndicatorFile(productFolder);

        return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Installed.");
    }

}
