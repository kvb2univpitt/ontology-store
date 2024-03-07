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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.TerminologiesType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductList;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Dec 5, 2023 6:01:17 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyFileService {

    private static final Log LOGGER = LogFactory.getLog(OntologyFileService.class);

    private final ObjectMapper objMapper = new ObjectMapper();

    private final FileSysService fileSysService;

    @Autowired
    public OntologyFileService(FileSysService fileSysService) {
        this.fileSysService = fileSysService;
    }

    public List<ProductType> getAvailableProducts(String downloadDirectory, String productListUrl) {
        List<ProductType> productDisplays = new LinkedList<>();

        try {
            getProducts(productListUrl).stream()
                    .map(productItem -> toProductTypes(downloadDirectory, productItem))
                    .forEach(productDisplays::add);
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return productDisplays;
    }

    private ProductType toProductTypes(String downloadDirectory, ProductItem productItem) {
        ProductType productType = new ProductType();
        productType.setId(productItem.getId());
        productType.setTitle(productItem.getTitle());
        productType.setVersion(productItem.getVersion());
        productType.setOwner(productItem.getOwner());
        productType.setType(productItem.getType());
        productType.setIncludeNetworkPackage(hasNetworkFiles(productItem.getNetworkFiles()));

        TerminologiesType terminologies = new TerminologiesType();
        terminologies.getTerminology().addAll(Arrays.asList(productItem.getTerminologies()));
        productType.setTerminologies(terminologies);

        getStatus(downloadDirectory, productType, productItem);

        return productType;
    }

    private boolean hasNetworkFiles(String[] networkFiles) {
        return !(networkFiles == null || networkFiles.length == 0);
    }

    private void getStatus(String downloadDirectory, ProductType product, ProductItem productItem) {
        String productFolder = product.getId();
        if (fileSysService.hasDirectory(downloadDirectory, productFolder)) {
            if (fileSysService.hasFinshedDownload(downloadDirectory, productFolder) && fileSysService.isProductFileExists(downloadDirectory, productItem)) {
                product.setDownloaded(true);
                product.setIncludeNetworkPackage(fileSysService.hasNetworkFiles(downloadDirectory, productFolder));

                if (fileSysService.hasFinshedInstall(downloadDirectory, productFolder)) {
                    product.setInstalled(true);
                    if (fileSysService.hasOntologyDisabled(downloadDirectory, productFolder)) {
                        product.setDisabled(true);
                    }
                } else if (fileSysService.hasFailedInstall(downloadDirectory, productFolder)) {
                    product.setFailed(true);
                    product.setStatusDetail(fileSysService.getFailedInstallMessage(downloadDirectory, productFolder));
                } else if (fileSysService.hasStartedInstall(downloadDirectory, productFolder)) {
                    product.setStarted(true);
                }
            } else if (fileSysService.hasFailedDownload(downloadDirectory, productFolder)) {
                product.setFailed(true);
                product.setStatusDetail(fileSysService.getFailedDownloadMessage(downloadDirectory, productFolder));
            } else if (fileSysService.hasStartedDownload(downloadDirectory, productFolder)) {
                product.setStarted(true);
            }
        }
    }

    public Map<String, ProductItem> getProductItems(String productListUrl) {
        try {
            return getProducts(productListUrl).stream()
                    .collect(Collectors.toMap(e -> e.getId(), Function.identity()));
        } catch (IOException exception) {
            return Collections.EMPTY_MAP;
        }
    }

    private List<ProductItem> getProducts(String productListUrl) throws IOException {
        List<ProductItem> productItems = new LinkedList<>();
        try {
            ProductList productList = objMapper.readValue(new URL(productListUrl), ProductList.class);
            if (productList != null) {
                productItems.addAll(productList.getProducts());
            }
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return productItems;
    }

}
