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

import edu.pitt.dbmi.ontology.store.ws.InstallActionException;
import edu.pitt.dbmi.ontology.store.ws.db.OntologyDBAccess;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
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

    private final FileSysService fileSysService;
    private final OntologyDBAccess ontologyDBAccess;

    @Autowired
    public OntologyInstallService(FileSysService fileSysService, OntologyDBAccess ontologyDBAccess) {
        this.fileSysService = fileSysService;
        this.ontologyDBAccess = ontologyDBAccess;
    }

    public void performInstallation(List<OntologyProductAction> actions) throws InstallActionException {
        // get a list of selected ontologies to install
        List<OntologyProductAction> ontologiesToInstall = actions.stream()
                .filter(e -> e.isInstall())
                .collect(Collectors.toList());

        // download ontologies, if any
        if (!ontologiesToInstall.isEmpty()) {
            for (OntologyProductAction action : ontologiesToInstall) {
                performValidation(action);

                String productFolder = action.getKey().replaceAll(".json", "");
                fileSysService.createStartedInstallIndicatorFile(productFolder);
                try {
                    performInstallation(action);
                } catch (InstallActionException exception) {
                    fileSysService.createFailedInstallIndicatorFile(productFolder);
                    throw exception;
                }
                fileSysService.createFinishedInstallIndicatorFile(productFolder);
            }
        }
    }

    private void performInstallation(OntologyProductAction action) throws InstallActionException {
        String productFolder = action.getKey().replaceAll(".json", "");

        List<Path> ontologies = fileSysService.getOntologies(productFolder);
        for (Path ontology : ontologies) {
            String fileName = ontology.getFileName().toString();
            String tableName = fileName.replaceAll(".tsv", "");

            try {
                ontologyDBAccess.createOntologyTable(tableName);
                ontologyDBAccess.insertIntoOntologyTable(ontology, tableName);
            } catch (SQLException | IOException exception) {
                LOGGER.error("SCHEMES.tsv insertion error.", exception);
                throw new InstallActionException(exception);
            }
        }

        try {
            ontologyDBAccess.insertIntoSchemesTable(fileSysService.getSchemesFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("SCHEMES.tsv insertion error.", exception);
            throw new InstallActionException(exception);
        }

        try {
            ontologyDBAccess.insertIntoTableAccessTable(fileSysService.getTableAccessFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("TABLE_ACCESS.tsv insertion error.", exception);
            throw new InstallActionException(exception);
        }
    }

    private synchronized void performValidation(OntologyProductAction action) throws InstallActionException {
        performDownloadStatusValidation(action);
        performInstallationStatusValidation(action);
        performDownloadFileValidation(action);
    }

    private void performDownloadFileValidation(OntologyProductAction action) throws InstallActionException {
        String title = action.getTitle();
        String productFolder = action.getKey().replaceAll(".json", "");

        if (Files.notExists(fileSysService.getSchemesFile(productFolder))) {
            throw new InstallActionException(String.format("'%s' SCHEMES.tsv not found on server.", action.getTitle()));
        }
        if (Files.notExists(fileSysService.getTableAccessFile(productFolder))) {
            throw new InstallActionException(String.format("'%s' TABLE_ACCESS.tsv not found on server.", action.getTitle()));
        }
        if (fileSysService.getOntologies(productFolder).isEmpty()) {
            throw new InstallActionException(String.format("No ontology files found for '%s'.", action.getTitle()));
        }
    }

    private void performDownloadStatusValidation(OntologyProductAction action) throws InstallActionException {
        String title = action.getTitle();
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);

        // check for ontology files
        if (Files.exists(productDir)) {
            if (Files.exists(fileSysService.getStartedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Download is in progress for '%s'.", action.getTitle());
                throw new InstallActionException(errMsg);
            } else if (Files.exists(fileSysService.getFailedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Download has previously failed for '%s'.  Please fix this.", action.getTitle());
                throw new InstallActionException(errMsg);
            } else if (Files.notExists(fileSysService.getFinishedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Unable to determine download status for '%s'.", action.getTitle());
                throw new InstallActionException(errMsg);
            }
        } else {
            throw new InstallActionException(String.format("'%s' has not been downloaded.  Please download the ontology first.", title));
        }

    }

    private void performInstallationStatusValidation(OntologyProductAction action) throws InstallActionException {
        String title = action.getTitle();
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);

        if (Files.exists(fileSysService.getStartedInstallIndicatorFile(productFolder))) {
            String errMsg = String.format("Installation is in progress for '%s'.", action.getTitle());
            throw new InstallActionException(errMsg);
        } else if (Files.exists(fileSysService.getFailedInstallIndicatorFile(productFolder))) {
            String errMsg = String.format("Installation has previously failed for '%s'.  Please fix this.", action.getTitle());
            throw new InstallActionException(errMsg);
        } else if (Files.exists(fileSysService.getFinishedInstallIndicatorFile(productFolder))) {
            String errMsg = String.format("Ontology '%s' has already been installed.", action.getTitle());
            throw new InstallActionException(errMsg);
        }
    }

}
