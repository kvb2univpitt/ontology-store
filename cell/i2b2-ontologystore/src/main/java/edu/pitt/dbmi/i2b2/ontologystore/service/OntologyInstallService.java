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

import edu.pitt.dbmi.i2b2.ontologystore.InstallationException;
import edu.pitt.dbmi.i2b2.ontologystore.ZipFileValidationException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.db.CrcDbSource;
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.db.OntDbSource;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.util.ZipFileUtils;
import edu.pitt.dbmi.i2b2.ontologystore.util.ZipFileValidation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 22, 2022 4:24:39 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyInstallService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final HiveDBAccess hiveDBAccess;

    private final MetadataInstallService metadataInstallService;
    private final CrcInstallService crcInstallService;
    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyInstallService(
            HiveDBAccess hiveDBAccess,
            MetadataInstallService metadataInstallService,
            CrcInstallService crcInstallService,
            OntologyFileService ontologyFileService) {
        this.hiveDBAccess = hiveDBAccess;
        this.metadataInstallService = metadataInstallService;
        this.crcInstallService = crcInstallService;
        this.ontologyFileService = ontologyFileService;
    }

    public void performInstallation(String projectId, String projectFolder, String downloadDirectory, List<ProductItem> productItemsToInstall) {
        productItemsToInstall = getValidDownloadedProductsToInstall(projectFolder, downloadDirectory, productItemsToInstall);
        if (!productItemsToInstall.isEmpty()) {
            try {
                OntDbSource ontDbSource = hiveDBAccess.getOntDbSource(projectId);
                CrcDbSource crcDbSource = hiveDBAccess.getCrcDbSource(projectId);
                if (ontDbSource == null || crcDbSource == null) {
                    throw new InstallationException(String.format("No i2b2 datasource associated with project '%s'.", projectId));
                }
                LOGGER.info(String.format("Using datasource %s on schema %s for project %s", ontDbSource.getJndiDatasource(), ontDbSource.getSchema(), projectId));
                LOGGER.info(String.format("Using datasource %s on schema %s for project %s", crcDbSource.getJndiDatasource(), crcDbSource.getSchema(), projectId));

                DataSource ontDataSource = getDataSource(ontDbSource.getJndiDatasource());
                DataSource crcDataSource = getDataSource(crcDbSource.getJndiDatasource());
                if (ontDataSource == null || crcDataSource == null) {
                    throw new InstallationException(String.format("No i2b2 JNDI datasource found for project '%s'.", projectId));
                }

                JdbcTemplate ontJdbcTemplate = new JdbcTemplate(ontDataSource);
                JdbcTemplate crcJdbcTemplate = new JdbcTemplate(crcDataSource);

                productItemsToInstall.forEach(productItem -> {
                    try {
                        install(ontDbSource, crcDbSource, projectFolder, downloadDirectory, productItem, ontJdbcTemplate, crcJdbcTemplate);
                    } catch (Exception exception) {
                        LOGGER.error("", exception);
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Metadata Installation Failed."));
                        ontologyFileService.setInstallFailed(Paths.get(downloadDirectory, productItem.getId(), projectFolder), exception.getMessage());
                    }
                });
            } catch (InstallationException exception) {
                LOGGER.error("Failed to install ontologies.", exception);
            }
        }
    }

    private void install(OntDbSource ontDbSource, CrcDbSource crcDbSource, String projectFolder, String downloadDirectory, ProductItem productItem, JdbcTemplate ontJdbcTemplate, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        Path productDir = Paths.get(downloadDirectory, productItem.getId());
        Path installDir = productDir.resolve(projectFolder);
        File productFile = ontologyFileService.getProductFile(productDir, productItem).toFile();
        try (ZipFile zipFile = new ZipFile(productFile)) {
            Map<String, ZipEntry> zipEntries = ZipFileUtils.getZipFileEntries(zipFile);

            ZipEntry packageJsonZipEntry = zipEntries.get("package.json");
            PackageFile packageFile = ZipFileUtils.getPackageFile(packageJsonZipEntry, zipFile);

            String rootFolder = new File(packageJsonZipEntry.getName()).getParent();

            ontologyFileService.setInstallStarted(installDir);
            try {
                metadataInstallService.createMetadata(ontDbSource, packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);
                metadataInstallService.insertIntoSchemesTable(ontDbSource, packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);
                metadataInstallService.insertIntoTableAccessTable(ontDbSource, packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);

                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Metadata Installed."));
            } catch (InstallationException exception) {
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Metadata Installation Failed."));

                throw exception;
            }

            try {
                crcInstallService.createConceptDimension(crcDbSource, packageFile, rootFolder, zipEntries, zipFile, crcJdbcTemplate);
                crcInstallService.insertIntoQtBreakdownPathTable(crcDbSource, packageFile, rootFolder, zipEntries, zipFile, crcJdbcTemplate);

                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "CRC Data Installed."));
            } catch (InstallationException exception) {
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "CRC Data Installation Failed."));

                throw exception;
            }
        } catch (IOException exception) {
            // this error occurs when the product file is not a zip file.
            throw new InstallationException("", exception);
        }

        ontologyFileService.setInstallFinished(installDir);
    }

    private List<ProductItem> getValidDownloadedProductsToInstall(String projectFolder, String downloadDirectory, List<ProductItem> downloadedProductItems) {
        List<ProductItem> productItems = new LinkedList<>();

        for (ProductItem productItem : downloadedProductItems) {
            String productFolder = productItem.getId();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            Path installDir = productDir.resolve(projectFolder);
            Path productFile = ontologyFileService.getProductFile(productDir, productItem);
            if (ontologyFileService.hasDirectory(productDir)) {
                if (ontologyFileService.isDownloadCompletelyFinshed(productDir, productFile)) {
                    if (ontologyFileService.isInstallFinshed(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Already Installed."));
                    } else if (ontologyFileService.isInstallFailed(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Installation previously failed."));
                    } else if (ontologyFileService.isInstallStarted(productDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, true, false, "Installation already started."));
                    } else {
                        ZipFileValidation zipFileValidation = new ZipFileValidation(ontologyFileService.getProductFile(productDir, productItem));
                        try {
                            zipFileValidation.validate();
                            productItems.add(productItem);
                        } catch (ZipFileValidationException exception) {
                            logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, exception.getMessage()));
                            ontologyFileService.setInstallFailed(installDir, exception.getMessage());
                        }
                    }
                } else if (ontologyFileService.isDownloadFailed(productDir)) {
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, ontologyFileService.getDownloadFailedMessage(productDir)));
                } else if (ontologyFileService.isDownloadStarted(productDir)) {
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Download not finished."));
                } else {
                    // download pending
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Download not started."));
                }
            } else {
                logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Has not been downloaded."));
            }
        }

        return productItems;
    }

    public List<ProductItem> getValidProductsToInstall(String projectFolder, String downloadDirectory, List<ProductActionType> actions, Map<String, ProductItem> products) {
        List<ProductItem> productItems = new LinkedList<>();

        for (ProductActionType action : actions) {
            if (!action.isInstall()) {
                continue;
            }

            String productFolder = action.getId();
            if (products.containsKey(productFolder)) {
                ProductItem productItem = products.get(productFolder);

                Path productDir = Paths.get(downloadDirectory, productFolder);
                if (ontologyFileService.hasDirectory(productDir)) {
                    Path installDir = productDir.resolve(projectFolder);
                    if (ontologyFileService.isInstallFinshed(installDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, true, "Already Installed."));
                    } else if (ontologyFileService.isInstallFailed(installDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Installation previously failed."));
                    } else if (ontologyFileService.isInstallStarted(installDir)) {
                        logActionSummary(createActionSummary(productItem, ACTION_TYPE, true, false, "Installation already started."));
                    } else {
                        // install pending
                        if (ontologyFileService.createDirectory(installDir) && ontologyFileService.setInstallPending(installDir)) {
                            productItems.add(productItem);
                        } else {
                            logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "Set install pending failed."));
                        }
                    }
                } else {
                    logActionSummary(createActionSummary(productItem, ACTION_TYPE, false, false, "No pending download."));
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
