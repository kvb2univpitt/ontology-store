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
    private final SchemesTableService schemesTableService;

    @Autowired
    public OntologyInstallService(FileSysService fileSysService, SchemesTableService schemesTableService) {
        this.fileSysService = fileSysService;
        this.schemesTableService = schemesTableService;
    }

    public void performInstallation(List<OntologyProductAction> actions) throws InstallActionException {
        // get a list of selected ontologies to install
        List<OntologyProductAction> listOfInstalls = actions.stream()
                .filter(e -> e.isInstall())
                .collect(Collectors.toList());

        if (!listOfInstalls.isEmpty()) {
            for (OntologyProductAction action : listOfInstalls) {
                performInstallation(action);
            }
        }
    }

    private void performInstallation(OntologyProductAction action) throws InstallActionException {
        performPreinstallValidation(action);

        String productFolder = action.getKey().replaceAll(".json", "");
        try {
            fileSysService.createInstallStartIndicatorFile(productFolder);

            // get files and folders
            Path productDir = fileSysService.getProductDirectory(productFolder);
            Path ontologyDir = fileSysService.getOntologyDirectory(productFolder);
            Path installStartIndicatorFile = fileSysService.getInstallStartIndicatorFile(productFolder);
            Path installFailedIndicatorFile = fileSysService.getInstallFailedIndicatorFile(productFolder);
            Path installFinishedIndicatorFile = fileSysService.getInstallFinishedIndicatorFile(productFolder);
            Path schemesFile = fileSysService.getSchemesFile(productFolder);
            Path tableAccessFile = fileSysService.getTableAccessFile(productFolder);

            try {
                schemesTableService.insert(schemesFile);
            } catch (SQLException exception) {
                LOGGER.error("SCHEMES.tsv insertion error.", exception);
                throw new InstallActionException(exception);
            }

            fileSysService.createInstallFinishedIndicatorFile(productFolder);
        } catch (Exception exception) {
            fileSysService.createInstallFailedIndicatorFile(productFolder);
            throw exception;
        }
    }

    private void performPreinstallValidation(OntologyProductAction action) throws InstallActionException {
        String productFolder = action.getKey().replaceAll(".json", "");

        // get files and folders
        Path productDir = fileSysService.getProductDirectory(productFolder);
        Path ontologyDir = fileSysService.getOntologyDirectory(productFolder);
        Path installStartIndicatorFile = fileSysService.getInstallStartIndicatorFile(productFolder);
        Path installFailedIndicatorFile = fileSysService.getInstallFailedIndicatorFile(productFolder);
        Path installFinishedIndicatorFile = fileSysService.getInstallFinishedIndicatorFile(productFolder);
        Path schemesFile = fileSysService.getSchemesFile(productFolder);
        Path tableAccessFile = fileSysService.getTableAccessFile(productFolder);

        if (Files.notExists(productDir)) {
            throw new InstallActionException(String.format("'%s' has not been downloaded.  Please download the ontology first.", action.getTitle()));
        }
        if (Files.exists(installStartIndicatorFile)) {
            throw new InstallActionException(String.format("'%s' installation has already started.", action.getTitle()));
        }
        if (Files.exists(installFailedIndicatorFile)) {
            throw new InstallActionException(String.format("'%s' installation has previously failed.  Please fix it.", action.getTitle()));
        }
        if (Files.exists(installFinishedIndicatorFile)) {
            throw new InstallActionException(String.format("'%s' has already been installed.", action.getTitle()));
        }
        if (Files.notExists(ontologyDir)) {
            throw new InstallActionException(String.format("'%s' ontology not found on server.", action.getTitle()));
        }
        if (Files.notExists(schemesFile)) {
            throw new InstallActionException(String.format("'%s' SCHEMES.tsv not found on server.", action.getTitle()));
        }
        if (Files.notExists(tableAccessFile)) {
            throw new InstallActionException(String.format("'%s' TABLE_ACCESS.tsv not found on server.", action.getTitle()));
        }

        try {
            List<Path> ontologies = Files.list(ontologyDir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            if (ontologies.isEmpty()) {
                throw new InstallActionException(String.format("No ontology files found for '%s'.", action.getTitle()));
            }
        } catch (IOException exception) {
            String errMsg = String.format("Unable to read '%s' ontology directory.", action.getTitle());
            LOGGER.error(errMsg, exception);
            throw new InstallActionException(errMsg);
        }
    }

}
