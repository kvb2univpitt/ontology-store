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
package edu.pitt.dbmi.ontology.store.ws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProduct;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductList;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import edu.pitt.dbmi.ontology.store.ws.model.SimpleOntologyStoreObject;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Mar 18, 2022 2:26:58 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class AmazonS3Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3Service.class);

    private static final Pattern SLASH_DELIMITER = Pattern.compile("/");

    private final String productListJsonUrl;
    private final FileSysService fileSysService;

    @Autowired
    public AmazonS3Service(
            @Value("${aws.s3.json.product.list}") String productListJsonUrl,
            FileSysService fileSysService) {
        this.productListJsonUrl = productListJsonUrl;
        this.fileSysService = fileSysService;
    }

    public List<OntologyProduct> getProducts() {
        List<OntologyProduct> products = new LinkedList<>();

        try {
            Map<String, SimpleOntologyStoreObject> objs = getSimpleOntologyStoreObjects();
            for (String fileName : objs.keySet()) {
                SimpleOntologyStoreObject obj = objs.get(fileName);

                // add product to list
                OntologyProduct product = new OntologyProduct();
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

    private void getStatus(OntologyProduct product) {
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

    private Map<String, SimpleOntologyStoreObject> getSimpleOntologyStoreObjects() throws IOException {
        Map<String, SimpleOntologyStoreObject> objs = new HashMap<>();

        ObjectMapper objMapper = new ObjectMapper();
        OntologyProductList productList = objMapper.readValue(new URL(productListJsonUrl), OntologyProductList.class);
        for (String productURL : productList.getProducts()) {
            String[] fields = SLASH_DELIMITER.split(productURL);
            objs.put(fields[fields.length - 1], objMapper.readValue(new URL(productURL), SimpleOntologyStoreObject.class));
        }

        return objs;
    }

    public OntologyStoreObject getOntologyStoreObject(String key) throws IOException {
        return getOntologyStoreObjects().get(key);
    }

    private Map<String, OntologyStoreObject> getOntologyStoreObjects() throws IOException {
        Map<String, OntologyStoreObject> objs = new HashMap<>();

        ObjectMapper objMapper = new ObjectMapper();
        OntologyProductList productList = objMapper.readValue(new URL(productListJsonUrl), OntologyProductList.class);
        for (String productURL : productList.getProducts()) {
            String[] fields = SLASH_DELIMITER.split(productURL);
            objs.put(fields[fields.length - 1], objMapper.readValue(new URL(productURL), OntologyStoreObject.class));
        }

        return objs;
    }

}
