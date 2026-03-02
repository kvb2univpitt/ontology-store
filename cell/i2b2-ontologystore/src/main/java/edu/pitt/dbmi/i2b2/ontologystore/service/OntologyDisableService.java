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
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.util.ZipFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
 * Nov 16, 2022 6:07:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyDisableService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyDisableService.class);

    private static final String DISABLE_ACTION_TYPE = "Disable";
    private static final String ENABLE_ACTION_TYPE = "Enable";

    private final HiveDBAccess hiveDBAccess;
    private final MetadataInstallService metadataInstallService;
    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyDisableService(HiveDBAccess hiveDBAccess, MetadataInstallService metadataInstallService, OntologyFileService ontologyFileService) {
        this.hiveDBAccess = hiveDBAccess;
        this.metadataInstallService = metadataInstallService;
        this.ontologyFileService = ontologyFileService;
    }

    public synchronized void performDisableEnable(String downloadDirectory, String project, String productListUrl, List<ProductActionType> actions, List<ActionSummaryType> summaries) throws InstallationException {
        // get actions that are marked for disable/enable
        actions = actions.stream().filter(ProductActionType::isDisableEnable).collect(Collectors.toList());

        List<ProductItem> productsToDisableEnable = getValidProductsToDisableEnable(downloadDirectory, actions, summaries, productListUrl);
        if (!productsToDisableEnable.isEmpty()) {
            String ontJNDIName = hiveDBAccess.getOntDataSourceJNDIName(project);
            if (ontJNDIName == null) {
                throw new InstallationException(String.format("No i2b2 datasource(s) associated with project '%s'.", project));
            }

            DataSource ontDataSource = getDataSource(ontJNDIName);
            if (ontDataSource == null) {
                throw new InstallationException(String.format("No i2b2 JNDI datasource(s) found for project '%s'.", project));
            }

            JdbcTemplate ontJdbcTemplate = new JdbcTemplate(ontDataSource);

            productsToDisableEnable.forEach(productItem -> {
                disableEnable(downloadDirectory, productItem, ontJdbcTemplate, summaries);
            });
        }
    }

    private void disableEnable(String downloadDirectory, ProductItem productItem, JdbcTemplate ontJdbcTemplate, List<ActionSummaryType> summaries) {
        String productFolder = productItem.getId();
        Path productDir = Paths.get(downloadDirectory, productFolder);
        File productFile = ontologyFileService.getProductFile(productDir, productItem).toFile();

        // active is true if previously disabled
        boolean active = ontologyFileService.isDisabled(productDir);

        try (ZipFile zipFile = new ZipFile(productFile)) {
            Map<String, ZipEntry> zipEntries = ZipFileUtils.getZipFileEntries(zipFile);

            ZipEntry packageJsonZipEntry = zipEntries.get("package.json");
            PackageFile packageFile = ZipFileUtils.getPackageFile(packageJsonZipEntry, zipFile);

            try {
                Map<String, String> attrs = metadataInstallService.getTableAccessVisualAttributes(packageFile, ontJdbcTemplate);
                attrs.forEach((k, v) -> attrs.replace(k, changeVisualAttribute(v, active)));

                metadataInstallService.updateTableAccessVisualAttributes(attrs, ontJdbcTemplate);

                if (active) {
                    ontologyFileService.setEnabled(productDir);
                    summaries.add(createActionSummary(productItem, ENABLE_ACTION_TYPE, false, true, "Enabled."));
                } else {
                    ontologyFileService.setDisabled(productDir);
                    summaries.add(createActionSummary(productItem, DISABLE_ACTION_TYPE, false, true, "Disabled."));
                }
            } catch (Exception exception) {
                String errMsg = active ? "Enable Ontology Failed." : "Disable Ontology Failed.";
                summaries.add(createActionSummary(productItem, DISABLE_ACTION_TYPE, false, false, errMsg));
            }
        } catch (IOException exception) {
            // this error occurs when the product file is not a zip file.
            LOGGER.error("", exception);
        }
    }

    private String changeVisualAttribute(String attribute, boolean active) {
        if (attribute == null || attribute.length() < 2) {
            return attribute;
        }

        char[] value = attribute.toCharArray();
        value[1] = active ? 'A' : 'H';

        return new String(value);
    }

    private List<ProductItem> getValidProductsToDisableEnable(
            String downloadDirectory,
            List<ProductActionType> actions,
            List<ActionSummaryType> summaries,
            String productListUrl) {
        List<ProductItem> validProductItems = new LinkedList<>();

        // get products from the disable list that are in the product list
        Map<String, ProductItem> productsToDisableEnable = new HashMap<>();
        Map<String, ProductItem> availableProducts = ontologyFileService.getUniqueProductItems(productListUrl);
        actions.forEach(action -> {
            String productId = action.getId();
            if (availableProducts.containsKey(productId)) {
                productsToDisableEnable.put(productId, availableProducts.get(productId));
            }
        });

        productsToDisableEnable.values().forEach(productItem -> {
            String productFolder = productItem.getId();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            Path productFile = ontologyFileService.getProductFile(productDir, productItem);

            if (ontologyFileService.isDownloadCompletelyFinshed(productDir, productFile) && ontologyFileService.isInstallFinshed(productDir)) {
                validProductItems.add(productItem);
            } else {
                summaries.add(createActionSummary(productItem, DISABLE_ACTION_TYPE, false, false, "Ontology not installed."));
            }
        });

        return validProductItems;
    }

}
