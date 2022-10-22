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
package edu.pitt.dbmi.i2b2.ontologystore.service;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItems;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 19, 2022 8:29:46 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyDownloadService {

    private static final Log LOGGER = LogFactory.getLog(OntologyDownloadService.class);

    private static final String ACTION_TYPE = "Download";

    private final AmazonS3Service amazonS3Service;
    private final FileSysService fileSysService;

    public OntologyDownloadService(AmazonS3Service amazonS3Service, FileSysService fileSysService) {
        this.amazonS3Service = amazonS3Service;
        this.fileSysService = fileSysService;
    }

    public synchronized void performDownload(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        actions = actions.stream().filter(e -> e.isDownload()).collect(Collectors.toList());
        actions = validate(actions, summaries);
        actions = prepare(actions, summaries);
        actions.stream().filter(e -> e.isDownload()).forEach(action -> summaries.add(download(action)));
    }

    private List<ProductActionType> validate(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductActionType> downloadActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            if (fileSysService.hasFinshedDownload(productFolder)) {
                ActionSummaryType summary = new ActionSummaryType();
                summary.setTitle(action.getTitle());
                summary.setActionType(ACTION_TYPE);
                summary.setInProgress(false);
                summary.setSuccess(true);
                summary.setDetail("Already downloaded.");
                summaries.add(summary);
            } else if (fileSysService.hasFailedDownload(productFolder)) {
                ActionSummaryType summary = new ActionSummaryType();
                summary.setTitle(action.getTitle());
                summary.setActionType(ACTION_TYPE);
                summary.setInProgress(false);
                summary.setSuccess(false);
                summary.setDetail("Download previously failed.");
                summaries.add(summary);
            } else if (fileSysService.hasStartedDownload(productFolder)) {
                ActionSummaryType summary = new ActionSummaryType();
                summary.setTitle(action.getTitle());
                summary.setActionType(ACTION_TYPE);
                summary.setInProgress(true);
                summary.setSuccess(false);
                summary.setDetail("Download already started.");
                summaries.add(summary);
            } else {
                downloadActions.add(action);
            }
        });

        return downloadActions;
    }

    private List<ProductActionType> prepare(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductActionType> downloadActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            Path productDir = fileSysService.getProductDirectory(productFolder);
            Path metadataDir = fileSysService.getMetadataDirectory(productFolder);
            Path crcDir = fileSysService.getCRCDirectory(productFolder);
            Path tableAccessDir = fileSysService.getTableAccessDirectory(productFolder);
            if (action.isIncludeNetworkPackage()) {
                Path networkDir = fileSysService.getNetworkDirectory(productFolder);
                if (fileSysService.createDirectories(productDir, metadataDir, crcDir, tableAccessDir, networkDir)) {
                    fileSysService.createDownloadStartedIndicatorFile(productFolder);
                    downloadActions.add(action);
                } else {
                    ActionSummaryType summary = new ActionSummaryType();
                    summary.setTitle(action.getTitle());
                    summary.setActionType(ACTION_TYPE);
                    summary.setInProgress(false);
                    summary.setSuccess(false);
                    summary.setDetail("Unable to create directories for download.");
                    summaries.add(summary);
                }

            } else {
                if (fileSysService.createDirectories(productDir, metadataDir, crcDir, tableAccessDir)) {
                    fileSysService.createDownloadStartedIndicatorFile(productFolder);
                    downloadActions.add(action);
                } else {
                    ActionSummaryType summary = new ActionSummaryType();
                    summary.setTitle(action.getTitle());
                    summary.setActionType(ACTION_TYPE);
                    summary.setInProgress(false);
                    summary.setSuccess(false);
                    summary.setDetail("Unable to create directories for download.");
                    summaries.add(summary);
                }
            }
        });

        return downloadActions;
    }

    private ActionSummaryType download(ProductActionType action) {
        String productFolder = action.getKey().replaceAll(".json", "");
        Path productDir = fileSysService.getProductDirectory(productFolder);
        Path metadataDir = fileSysService.getMetadataDirectory(productFolder);
        Path crcDir = fileSysService.getCRCDirectory(productFolder);
        Path tableAccessDir = fileSysService.getTableAccessDirectory(productFolder);
        try {
            ProductItems storeObject = amazonS3Service.getProductItemsObject(action.getKey());
            if (storeObject != null) {
                downloadFile(storeObject.getSchemes(), productDir);
                downloadFile(storeObject.getBreakdownPath(), productDir);
                if (action.isIncludeNetworkPackage()) {
                    Path networkDir = fileSysService.getNetworkDirectory(productFolder);
                    downloadFile(storeObject.getAdapterMapping(), networkDir);
                }

                for (String domainOntologyURI : storeObject.getListOfDomainOntologies()) {
                    downloadFile(domainOntologyURI, metadataDir);
                }

                for (String conceptDimensionURI : storeObject.getConceptDimensions()) {
                    downloadFile(conceptDimensionURI, crcDir);
                }

                for (String tableAccessURI : storeObject.getTableAccess()) {
                    downloadFile(tableAccessURI, tableAccessDir);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("", exception);
            fileSysService.createDownloadFailedIndicatorFile(productFolder);

            ActionSummaryType summary = new ActionSummaryType();
            summary.setTitle(action.getTitle());
            summary.setActionType(ACTION_TYPE);
            summary.setInProgress(false);
            summary.setSuccess(false);
            summary.setDetail("Download Failed!");

            return summary;
        }
        fileSysService.createDownloadFinishedIndicatorFile(productFolder);

        ActionSummaryType summary = new ActionSummaryType();
        summary.setTitle(action.getTitle());
        summary.setActionType(ACTION_TYPE);
        summary.setInProgress(false);
        summary.setSuccess(true);
        summary.setDetail("Downloaded.");

        return summary;
    }

    private static void downloadFile(String uri, Path productDir) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(productDir.toString(), fileName);

        try ( InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
