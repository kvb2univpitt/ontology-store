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
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 *
 * Oct 22, 2022 4:24:39 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyInstallService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final HiveDBAccess hiveDBAccess;

    public OntologyInstallService(HiveDBAccess hiveDBAccess, FileSysService fileSysService, OntologyFileService ontologyFileService) {
        super(fileSysService, ontologyFileService);
        this.hiveDBAccess = hiveDBAccess;
    }

    public synchronized void performInstallation(String project, List<ProductActionType> actions, List<ActionSummaryType> summaries) throws InstallationException {
        // get actions that are marked for install
        actions = actions.stream().filter(ProductActionType::isInstall).collect(Collectors.toList());

        List<ProductItem> productsToInstall = getValidProductsToInstall(actions, summaries);
        for (ProductItem productItem : productsToInstall) {
            install(productItem, project, summaries);
        }
    }

    private void install(ProductItem productItem, String project, List<ActionSummaryType> summaries) throws InstallationException {
        String ontJNDIName = hiveDBAccess.getOntDataSourceJNDIName(project);
        String crcJNDIName = hiveDBAccess.getCrcDataSourceJNDIName(project);
        if (ontJNDIName == null || crcJNDIName == null) {
            throw new InstallationException(String.format("No i2b2 datasource(s) associated with project '%s'.", project));
        }

        DataSource ontDataSource = getDataSource(ontJNDIName);
        DataSource crcDataSource = getDataSource(crcJNDIName);
        if (ontDataSource == null || crcDataSource == null) {
            throw new InstallationException(String.format("No i2b2 JNDI datasource(s) found for project '%s'.", project));
        }
    }

    private List<ProductItem> getValidProductsToInstall(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductItem> validProductItems = new LinkedList<>();

        // get products from the install list that are in the product list
        Map<String, ProductItem> productsToInstall = new HashMap<>();
        Map<String, ProductItem> availableProducts = ontologyFileService.getProductItems();
        actions.forEach(action -> {
            String productId = action.getId();
            if (availableProducts.containsKey(productId)) {
                productsToInstall.put(productId, availableProducts.get(productId));
            }
        });

        productsToInstall.values().forEach(productItem -> {
            String productFolder = productItem.getId();
            String title = productItem.getTitle();
            if (fileSysService.hasFinshedDownload(productFolder) && fileSysService.isProductrFileExists(productItem, productFolder)) {
                if (fileSysService.hasFinshedInstall(productFolder)) {
                    summaries.add(createActionSummary(title, ACTION_TYPE, false, true, "Already Installed."));
                } else if (fileSysService.hasFailedInstall(productFolder)) {
                    summaries.add(createActionSummary(title, ACTION_TYPE, false, false, "Installation previously failed."));
                } else if (fileSysService.hasStartedInstall(productFolder)) {
                    summaries.add(createActionSummary(title, ACTION_TYPE, true, false, "Installation already started."));
                } else {
                    validProductItems.add(productItem);
                }
            } else if (fileSysService.hasFailedDownload(productFolder)) {
                summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, fileSysService.getFailedDownloadMessage(productFolder)));
            } else if (fileSysService.hasStartedDownload(productFolder)) {
                summaries.add(createActionSummary(title, ACTION_TYPE, false, false, "Download not finished."));
            } else {
                summaries.add(createActionSummary(title, ACTION_TYPE, false, false, "Has not been downloaded."));
            }
        });

        return validProductItems;
    }

    private DataSource getDataSource(String datasourceJNDIName) {
        try {
            return (new JndiDataSourceLookup()).getDataSource(datasourceJNDIName);
        } catch (Exception exception) {
            String errMsg = String.format("Unable to get datasource for JNDI name '%s'.", datasourceJNDIName);
            LOGGER.error(errMsg, exception);
            return null;
        }
    }

}
