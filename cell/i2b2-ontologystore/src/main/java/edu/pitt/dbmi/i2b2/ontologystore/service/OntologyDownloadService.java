/*
 * Copyright (C) 2024 University of Pittsburgh.
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
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 6, 2023 7:02:12 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyDownloadService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyDownloadService.class);

    private static final String ACTION_TYPE = "Download";

    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyDownloadService(OntologyFileService ontologyFileService) {
        this.ontologyFileService = ontologyFileService;
    }

    public synchronized void performDownload(String downloadDirectory, List<ProductItem> productItemsToDownload) {
        List<ProductItem> downloadedProductItems = downloadFiles(downloadDirectory, productItemsToDownload);
        verifyFileIntegrity(downloadDirectory, downloadedProductItems);
    }

    /**
     * Verify the integrity of the downloaded products by computing the SHA-256
     * checksum and compare it with the ones given in the product list.
     *
     * @param productsToDownload
     * @param summaries
     */
    private void verifyFileIntegrity(String downloadDirectory, List<ProductItem> downloadedProductItems) {
        downloadedProductItems.forEach(productItem -> {
            String productFolder = productItem.getId();

            String fileURI = productItem.getFile();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            String sha256Checksum = ontologyFileService.createSha256Checksum(productDir, fileURI);
            if (sha256Checksum.compareTo(productItem.getSha256Checksum()) == 0) {
                ontologyFileService.setDownloadFinished(productDir);
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Downloaded successfully."));
            } else {
                String errorMsg = "File verification failed.  SHA-256 checksum does not match.";
                ontologyFileService.setDownloadFailed(productDir, errorMsg);
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, errorMsg));
            }
        });
    }

    /**
     * Download production from the given product list.
     *
     * @param productsToDownload a list of products to download
     * @param summaries a list to store download summaries
     * @return a list products that are successfully downloaded
     */
    private List<ProductItem> downloadFiles(String downloadDirectory, List<ProductItem> productsToDownload) {
        List<ProductItem> downloadedProducts = new LinkedList<>();

        productsToDownload.forEach(productItem -> {
            String productFolder = productItem.getId();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            if (ontologyFileService.hasDirectory(productDir) && ontologyFileService.setDownloadStarted(productDir)) {
                try {
                    // download product file
                    ontologyFileService.downloadFile(productItem.getFile(), productDir);

                    //download network files, if any
                    String[] networkFiles = productItem.getNetworkFiles();
                    if (hasNetworkFiles(networkFiles)) {
                        Path networkDir = Paths.get(productDir.toString(), "network_files");
                        if (ontologyFileService.createDirectory(networkDir)) {
                            try {
                                for (String networkFile : networkFiles) {
                                    ontologyFileService.downloadFile(networkFile, networkDir);
                                }
                            } catch (IOException exception) {
                                LOGGER.error("", exception);
                            }
                        } else {
                            logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Unable to download adapter mapping files."));
                        }
                    }
                    downloadedProducts.add(productItem);
                } catch (Exception exception) {
                    LOGGER.error("", exception);
                    String errorMsg = "Unable to download from the given URL.";
                    ontologyFileService.setDownloadFailed(productDir, errorMsg);
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, errorMsg));
                }
            } else {
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Unable to create directories for download."));
            }
        });

        return downloadedProducts;
    }

    private boolean hasNetworkFiles(String[] networkFiles) {
        return !(networkFiles == null || networkFiles.length == 0);
    }

    /**
     * Get a list of product items based on the download-action list that meet
     * the following conditions:
     *
     * <ul>
     * <li>Is not pending for download.</li>
     * <li>Have not been downloaded.</li>
     * <li>Have not previous failed to download.</li>
     * <li>Are not currently downloading</li>
     * </ul>
     *
     * @param downloadDirectory directory to download product package
     * @param productListUrl URL to product list on the cloud
     * @param actions a list of download actions
     * @param products unique products from the product list on cloud
     * @return a list of products to download
     */
    public List<ProductItem> getValidProductsToDownload(String downloadDirectory, List<ProductActionType> actions, Map<String, ProductItem> products) {
        List<ProductItem> productItems = new LinkedList<>();

        for (ProductActionType action : actions) {
            if (!action.isDownload()) {
                continue;
            }

            String productFolder = action.getId();
            if (products.containsKey(productFolder)) {
                ProductItem productItem = products.get(productFolder);

                Path productDir = Paths.get(downloadDirectory, productFolder);
                Path productFile = ontologyFileService.getProductFile(productDir, productItem);
                if (ontologyFileService.hasDirectory(productDir)) {
                    if (ontologyFileService.isDownloadPending(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Download already pending."));
                    } else if (ontologyFileService.isDownloadCompletelyFinshed(productDir, productFile)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Already downloaded."));
                    } else if (ontologyFileService.isDownloadFailed(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, ontologyFileService.getDownloadFailedMessage(productDir)));
                    } else if (ontologyFileService.isDownloadStarted(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, true, false, "Download already started."));
                    }

                    continue;
                }

                if (!action.isIncludeNetworkPackage()) {
                    productItem.setNetworkFiles(new String[0]);
                }

                if (ontologyFileService.createDirectory(productDir) && ontologyFileService.setDownloadPending(productDir)) {
                    productItems.add(productItem);
                } else {
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Unable create directory for download."));
                }
            }
        }

        return productItems;
    }

    private void logActionSummary(ActionSummaryType summary) {
        LOGGER.info(String.format("id=%s, title=%s, action=%s, in progress=%s, success=%s, detail=%s",
                summary.getId(), summary.getTitle(), summary.getActionType(), summary.isInProgress(), summary.isSuccess(), summary.getDetail()));
    }

}
