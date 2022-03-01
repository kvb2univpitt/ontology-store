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

import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
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

    private static final String ACTION_TYPE = "Download";

    private final AmazonS3Service amazonS3Service;
    private final FileSysService fileSysService;

    @Autowired
    public OntologyDownloadService(AmazonS3Service amazonS3Service, FileSysService fileSysService) {
        this.amazonS3Service = amazonS3Service;
        this.fileSysService = fileSysService;
    }

    public synchronized void performDownload(List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        actions = actions.stream().filter(e -> e.isDownload()).collect(Collectors.toList());
        actions = validate(actions, summaries);
        actions = prepare(actions, summaries);
        actions.stream().filter(e -> e.isDownload()).forEach(action -> summaries.add(download(action)));
    }

    private List<OntologyProductAction> validate(List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        List<OntologyProductAction> downloadActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            if (fileSysService.hasFinshedDownload(productFolder)) {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Already downloaded."));
            } else if (fileSysService.hasFailedDownload(productFolder)) {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download previously failed."));
            } else if (fileSysService.hasStartedDownload(productFolder)) {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, true, false, "Download already started."));
            } else {
                downloadActions.add(action);
            }
        });

        return downloadActions;
    }

    private List<OntologyProductAction> prepare(List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        List<OntologyProductAction> downloadActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            Path productDir = fileSysService.getProductDirectory(productFolder);
            Path metadataDir = fileSysService.getMetadataDirectory(productFolder);
            Path crcDir = fileSysService.getCRCDirectory(productFolder);
            if (action.isIncludeNetworkPackage()) {
                Path networkDir = fileSysService.getNetworkDirectory(productFolder);
                if (fileSysService.createDirectories(productDir, metadataDir, crcDir, networkDir)) {
                    fileSysService.createDownloadStartedIndicatorFile(productFolder);
                    downloadActions.add(action);
                } else {
                    summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Unable to create directories for download."));
                }

            } else {
                if (fileSysService.createDirectories(productDir, metadataDir, crcDir)) {
                    fileSysService.createDownloadStartedIndicatorFile(productFolder);
                    downloadActions.add(action);
                } else {
                    summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Unable to create directories for download."));
                }
            }
        });

        return downloadActions;
    }

    private ActionSummary download(OntologyProductAction action) {
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);
        Path metadataDir = fileSysService.getMetadataDirectory(productFolder);
        Path crcDir = fileSysService.getCRCDirectory(productFolder);
        try {
            OntologyStoreObject storeObject = amazonS3Service.getOntologyStoreObject(action.getKey());
            if (storeObject != null) {
                downloadFile(storeObject.getSchemes(), productDir);
                downloadFile(storeObject.getTableAccess(), productDir);
                downloadFile(storeObject.getBreakdownPath(), productDir);
                if (action.isIncludeNetworkPackage()) {
                    Path networkDir = fileSysService.getNetworkDirectory(productFolder);
                    downloadFile(storeObject.getAdapterMapping(), networkDir);
                }

                String[] domainOntologies = storeObject.getListOfDomainOntologies();
                for (String domainOntologyURI : domainOntologies) {
                    downloadFile(domainOntologyURI, metadataDir);
                }

                String[] conceptDimensions = storeObject.getConceptDimensions();
                for (String conceptDimensionURI : conceptDimensions) {
                    downloadFile(conceptDimensionURI, crcDir);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("", exception);
            fileSysService.createDownloadFailedIndicatorFile(productFolder);

            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download Failed!");
        }
        fileSysService.createDownloadFinishedIndicatorFile(productFolder);

        return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Downloaded.");
    }

    private static void downloadFile(String uri, Path productDir) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(productDir.toString(), fileName);

        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
