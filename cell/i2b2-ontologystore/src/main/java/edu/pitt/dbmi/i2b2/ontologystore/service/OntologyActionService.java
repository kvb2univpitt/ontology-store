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

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 2, 2026 10:53:59 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyActionService {

    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyActionService(OntologyFileService ontologyFileService) {
        this.ontologyFileService = ontologyFileService;
    }

    public void executeActions(Map<String, ProductItem> products, List<ProductActionType> actions, String downloadDirectory) {
        createPendingActions(downloadDirectory, products, actions);
    }

    private void createPendingActions(String downloadDirectory, Map<String, ProductItem> products, List<ProductActionType> actions) {
        for (ProductActionType action : actions) {
            ProductItem productItem = products.get(action.getId());

            String productFolder = productItem.getId();
            Path productDir = Paths.get(downloadDirectory, productFolder);
            Path productFile = ontologyFileService.getProductFile(productDir, productItem);

            if (ontologyFileService.createDirectoryIfNotExists(productDir)) {
                if (action.isDownload()) {
                    ontologyFileService.createDownloadPendingFile(productDir);
                }
                if (action.isInstall()) {
                    ontologyFileService.createInstallPendingFile(productDir);
                }
            }
        }
    }

}
