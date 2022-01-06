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

import edu.pitt.dbmi.ontology.store.ws.DownloadActionException;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 7, 2021 1:23:48 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyDownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyDownloadService.class);

    private final AmazonS3Service amazonS3Service;
    private final FileSysService fileSysService;

    @Autowired
    public OntologyDownloadService(AmazonS3Service amazonS3Service, FileSysService fileSysService) {
        this.amazonS3Service = amazonS3Service;
        this.fileSysService = fileSysService;
    }

    public void performDownload(List<OntologyProductAction> actions) throws DownloadActionException {
        // get a list of selected ontologies to download
        List<OntologyProductAction> ontologiesToDownload = actions.stream()
                .filter(e -> e.isDownload())
                .collect(Collectors.toList());

        // download ontologies, if any
        if (!ontologiesToDownload.isEmpty()) {
            for (OntologyProductAction action : ontologiesToDownload) {
                performValidation(action);

                String productFolder = action.getKey().replaceAll(".json", "");
                try {
                    performProductDownload(action);
                } catch (DownloadActionException exception) {
                    fileSysService.createFailedDownloadIndicatorFile(productFolder);
                    throw exception;
                }
                fileSysService.createFinishedDownloadIndicatorFile(productFolder);
            }
        }
    }

    private void performProductDownload(OntologyProductAction action) throws DownloadActionException {
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);
        try {
            OntologyStoreObject storeObject = amazonS3Service.getOntologyStoreObject(action.getKey());
            if (storeObject != null) {
                downloadFile(storeObject.getSchemes(), productDir);
                downloadFile(storeObject.getTableAccess(), productDir);

                String[] domainOntologies = storeObject.getListOfDomainOntologies();
                if (domainOntologies.length > 0) {
                    Path ontologyDir = fileSysService.getOntologyDirectory(productFolder);
                    for (String domainOntologyURI : domainOntologies) {
                        downloadFile(domainOntologyURI, ontologyDir);
                    }
                }
            }
        } catch (IOException exception) {
            fileSysService.createFailedDownloadIndicatorFile(productFolder);

            String errMsg = String.format("Unable to dowload '%s'.", action.getTitle());
            LOGGER.error(errMsg, exception);

            throw new DownloadActionException(errMsg);
        } catch (DownloadActionException exception) {
            fileSysService.createFailedDownloadIndicatorFile(productFolder);

            throw exception;
        }
    }

    private static void downloadFile(String uri, Path productDir) throws DownloadActionException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(productDir.toString(), fileName);

        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            String errMsg = String.format("Unable to download file %s.", fileName);
            LOGGER.error(errMsg, exception);

            throw new DownloadActionException(errMsg);
        }
    }

    private synchronized void performValidation(OntologyProductAction action) throws DownloadActionException {
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);
        if (Files.exists(productDir)) {
            if (Files.exists(fileSysService.getStartedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Download is in progress for '%s'.", action.getTitle());
                throw new DownloadActionException(errMsg);
            } else if (Files.exists(fileSysService.getFinishedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Ontology'%s' has already been downloaded.", action.getTitle());
                throw new DownloadActionException(errMsg);
            } else if (Files.exists(fileSysService.getFailedDownloadIndicatorFile(productFolder))) {
                String errMsg = String.format("Download has previously failed for '%s'.  Please fix this.", action.getTitle());
                throw new DownloadActionException(errMsg);
            } else {
                String errMsg = String.format("Unable to determine download status for '%s'.", action.getTitle());
                throw new DownloadActionException(errMsg);
            }
        } else {
            Path ontologyDir = fileSysService.getOntologyDirectory(productFolder);
            if (fileSysService.createDirectories(productDir) && fileSysService.createDirectories(ontologyDir)) {
                fileSysService.createStartedDownloadIndicatorFile(productFolder);
            } else {
                String errMsg = String.format("Unable to create download folder for '%s'.", action.getTitle());
                throw new DownloadActionException(errMsg);
            }
        }
    }

}
