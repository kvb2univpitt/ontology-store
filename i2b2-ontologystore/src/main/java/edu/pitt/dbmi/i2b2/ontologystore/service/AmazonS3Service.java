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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductList;
import edu.pitt.dbmi.i2b2.ontologystore.model.SimpleProduct;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 18, 2022 4:35:17 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class AmazonS3Service {

    private static final Log LOGGER = LogFactory.getLog(AmazonS3Service.class);

    private static final Pattern SLASH_DELIMITER = Pattern.compile("/");

    private final String productListJsonUrl;
    private final FileSysService fileSysService;

    public AmazonS3Service(String productListJsonUrl, FileSysService fileSysService) {
        this.productListJsonUrl = productListJsonUrl;
        this.fileSysService = fileSysService;
    }

    public List<ProductType> getProducts() {
        List<ProductType> products = new LinkedList<>();

        try {
            Map<String, SimpleProduct> objs = SimpleProducts();
            for (String fileName : objs.keySet()) {
                SimpleProduct obj = objs.get(fileName);

                // add product to list
                ProductType product = new ProductType();
                product.setFileName(fileName);
                product.setTitle(obj.getProductTitle());
                product.setVersion(obj.getProductVersion());
                product.setOwner(obj.getProductOwner());
                product.setType(obj.getProductType());
                product.setIncludeNetworkPackage(obj.getIncludeNetworkPackage().equals("Y"));
                product.setTerminologies(obj.getTerminologies());

                getStatus(product);

                products.add(product);
            }
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return products;
    }

    private void getStatus(ProductType product) {
        String productFolder = product.getFileName().replaceAll(".json", "");
        if (fileSysService.hasFinshedDownload(productFolder)) {
            product.setDownloaded(true);
            product.setIncludeNetworkPackage(fileSysService.hasNetworkFiles(productFolder));

            if (fileSysService.hasFinshedInstall(productFolder)) {
                product.setInstalled(true);
            } else if (fileSysService.hasFailedInstall(productFolder)) {
                product.setFailed(true);
            } else if (fileSysService.hasStartedInstall(productFolder)) {
                product.setStarted(true);
            }
        } else if (fileSysService.hasFailedDownload(productFolder)) {
            product.setFailed(true);
        } else if (fileSysService.hasStartedDownload(productFolder)) {
            product.setStarted(true);
        }
    }

    private Map<String, SimpleProduct> SimpleProducts() throws IOException {
        Map<String, SimpleProduct> objs = new TreeMap<>();

        ObjectMapper objMapper = new ObjectMapper();
        ProductList productList = objMapper.readValue(new URL(productListJsonUrl), ProductList.class);
        for (String productURL : productList.getProducts()) {
            String[] fields = SLASH_DELIMITER.split(productURL);
            objs.put(fields[fields.length - 1], objMapper.readValue(new URL(productURL), SimpleProduct.class));
        }
        return objs;
    }

}
