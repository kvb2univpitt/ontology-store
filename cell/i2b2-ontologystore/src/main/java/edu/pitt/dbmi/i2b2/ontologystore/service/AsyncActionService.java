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

import edu.pitt.dbmi.i2b2.ontologystore.InstallationException;
import edu.pitt.dbmi.i2b2.ontologystore.ZipFileValidationException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.util.ZipFileValidation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 9, 2026 3:15:50 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class AsyncActionService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(AsyncActionService.class);

    private static final String ACTION_TYPE = "Install";
    private static final int NUM_THREAD = 4;

    private final OntologyDownloadService ontologyDownloadService;
    private final OntologyInstallService ontologyInstallService;
    private final OntologyFileService ontologyFileService;

    @Autowired
    public AsyncActionService(OntologyDownloadService ontologyDownloadService, OntologyInstallService ontologyInstallService, OntologyFileService ontologyFileService) {
        this.ontologyDownloadService = ontologyDownloadService;
        this.ontologyInstallService = ontologyInstallService;
        this.ontologyFileService = ontologyFileService;
    }

    @Async
    public CompletableFuture<List<ActionSummaryType>> performActions(
            String projectId,
            String downloadDirectory,
            String productListUrl,
            List<ProductActionType> actions) {
        List<ActionSummaryType> summaries = new LinkedList<>();

        ontologyDownloadService.performDownload(downloadDirectory, productListUrl, actions, summaries);

        performInstall(projectId, downloadDirectory, productListUrl, actions, summaries);

        return CompletableFuture.completedFuture(summaries);
    }

    private void performInstall(
            String projectId,
            String downloadDirectory,
            String productListUrl,
            List<ProductActionType> actions,
            List<ActionSummaryType> summaries) {
        List<ProductActionType> installActions = actions.stream().filter(ProductActionType::isInstall).collect(Collectors.toList());
        List<ProductItem> productsToInstall = getValidProductsToInstall(downloadDirectory, productListUrl, installActions, summaries);

        // prepare for installation
        productsToInstall.stream()
                .map(ProductItem::getId)
                .map(productFolder -> Paths.get(downloadDirectory, productFolder))
                .forEach(productDir -> ontologyFileService.setInstallStarted(productDir));

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);
        productsToInstall.forEach(productItem -> {
            executor.submit(() -> {
                try {
                    ontologyInstallService.performInstallation(downloadDirectory, projectId, productListUrl, productItem, summaries);
                } catch (InstallationException exception) {
                    LOGGER.error("Failed to install ontologies.", exception);
                }
            });
        });
        shutdownAndAwaitTermination(executor);
    }

    private List<ProductItem> getValidProductsToInstall(String downloadDirectory, String productListUrl, List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductItem> validProductItems = new LinkedList<>();

        // get products from the install list that are in the product list
        Map<String, ProductItem> productsToInstall = new HashMap<>();
        Map<String, ProductItem> availableProducts = ontologyFileService.getProductItems(productListUrl);
        actions.forEach(action -> {
            String productId = action.getId();
            if (availableProducts.containsKey(productId)) {
                productsToInstall.put(productId, availableProducts.get(productId));
            }
        });

        productsToInstall.values().forEach(productItem -> {
            String productFolder = productItem.getId();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            Path productFile = ontologyFileService.getProductFile(productDir, productItem);

            if (ontologyFileService.hasDirectory(productDir)) {
                if (ontologyFileService.isDownloadCompletelyFinshed(productDir, productFile)) {
                    if (ontologyFileService.isInstallFinshed(productDir)) {
                        summaries.add(createActionSummary(productItem, ACTION_TYPE, false, true, "Already Installed."));
                    } else if (ontologyFileService.isInstallFailed(productDir)) {
                        summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Installation previously failed."));
                    } else if (ontologyFileService.isInstallStarted(productDir)) {
                        summaries.add(createActionSummary(productItem, ACTION_TYPE, true, false, "Installation already started."));
                    } else {
                        ZipFileValidation zipFileValidation = new ZipFileValidation(ontologyFileService.getProductFile(productDir, productItem));
                        try {
                            zipFileValidation.validate();
                            validProductItems.add(productItem);
                        } catch (ZipFileValidationException exception) {
                            summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, exception.getMessage()));
                            ontologyFileService.setInstallFailed(productDir, exception.getMessage());
                        }
                    }
                } else if (ontologyFileService.isDownloadFailed(productDir)) {
                    summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, ontologyFileService.getDownloadFailedMessage(productDir)));
                } else if (ontologyFileService.isDownloadStarted(productDir)) {
                    summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Download not finished."));
                } else {
                    summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Has not been downloaded."));
                }
            } else {
                summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Has not been downloaded."));
            }
        });

        return validProductItems;
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            boolean isTerminated = pool.awaitTermination(24, TimeUnit.HOURS);

            // force shutdown if the executor is not terminated
            if (!isTerminated) {
                pool.shutdownNow();
                if (!pool.awaitTermination(6000, TimeUnit.MILLISECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException exception) {
            pool.shutdownNow();

            LOGGER.error(exception.getMessage());
        }
    }

}
