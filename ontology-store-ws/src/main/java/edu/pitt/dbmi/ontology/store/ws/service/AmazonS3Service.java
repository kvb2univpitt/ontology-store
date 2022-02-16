/*
 * Copyright (C) 2021 University of Pittsburgh.
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProduct;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 7, 2021 2:51:12 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class AmazonS3Service {

    private final String ontologyBucketName;
    private final String ontologyKeyName;

    private final String networkBucketName;
    private final String networkKeyName;

    private final AmazonS3 amazonS3;
    private final FileSysService fileSysService;

    @Autowired
    public AmazonS3Service(
            @Value("${ontology.aws.s3.bucket.name}") String ontologyBucketName,
            @Value("${ontology.aws.s3.key.name}") String ontologyKeyName,
            @Value("${network.aws.s3.bucket.name}") String networkBucketName,
            @Value("${network.aws.s3.key.name}") String networkKeyName,
            AmazonS3 amazonS3,
            FileSysService fileSysService) {
        this.ontologyBucketName = ontologyBucketName;
        this.ontologyKeyName = ontologyKeyName;
        this.networkBucketName = networkBucketName;
        this.networkKeyName = networkKeyName;
        this.amazonS3 = amazonS3;
        this.fileSysService = fileSysService;
    }

    public List<OntologyProduct> getProducts() {
        List<OntologyProduct> products = new LinkedList<>();

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ontologyBucketName)
                .withPrefix(ontologyKeyName + "/")
                .withDelimiter("/");

        ObjectMapper objectMapper = new ObjectMapper();
        amazonS3.listObjectsV2(request)
                .getObjectSummaries().stream()
                .filter(objSummary -> objSummary.getKey().endsWith(".json"))
                .forEach(objSummary -> {
                    S3Object s3Obj = amazonS3.getObject(objSummary.getBucketName(), objSummary.getKey());
                    try (BufferedInputStream in = new BufferedInputStream(s3Obj.getObjectContent())) {
                        OntologyStoreObject obj = objectMapper.readValue(in, OntologyStoreObject.class);

                        // add product to list
                        OntologyProduct product = new OntologyProduct();
                        product.setFileName(objSummary.getKey());
                        product.setTitle(obj.getProductTitle());
                        product.setVersion(obj.getProductVersion());
                        product.setOwner(obj.getProductOwner());
                        product.setType(obj.getProductType());
                        product.setIncludeNetworkPackage(obj.getIncludeNetworkPackage().equals("Y"));
                        product.setTerminologies(obj.getTerminologies());

                        getStatus(product);

                        products.add(product);
                    } catch (IOException exception) {
                        exception.printStackTrace(System.err);
                    }
                });

        return products;
    }

    public void downloadNetworkFiles(Path dir) throws IOException {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(networkBucketName)
                .withPrefix(networkKeyName + "/")
                .withDelimiter("/");

        for (S3ObjectSummary objSummary : amazonS3.listObjectsV2(request).getObjectSummaries()) {
            S3Object s3Obj = amazonS3.getObject(objSummary.getBucketName(), objSummary.getKey());
            if (objSummary.getSize() > 0) {
                Path out = Paths.get(dir.toString(), s3Obj.getKey());
                try (BufferedInputStream in = new BufferedInputStream(s3Obj.getObjectContent())) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
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

    public List<OntologyStoreObject> listOntologyStoreObject() {
        List<OntologyStoreObject> ontologyStoreObjects = new LinkedList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(ontologyBucketName)
                .withPrefix(ontologyKeyName + "/")
                .withDelimiter("/");
        ListObjectsV2Result response = amazonS3.listObjectsV2(request);
        response.getObjectSummaries().stream()
                .filter(objSummary -> objSummary.getKey().endsWith(".json"))
                .map(objSummary -> amazonS3.getObject(objSummary.getBucketName(), objSummary.getKey()))
                .forEach(s3Obj -> {
                    try (BufferedInputStream in = new BufferedInputStream(s3Obj.getObjectContent())) {
                        OntologyStoreObject ontologyStoreObject = objectMapper.readValue(in, OntologyStoreObject.class);

                        ontologyStoreObjects.add(ontologyStoreObject);
                    } catch (IOException exception) {
                        exception.printStackTrace(System.err);
                    }
                });

        return ontologyStoreObjects;
    }

    public OntologyStoreObject getOntologyStoreObject(String key) throws IOException {
        S3Object s3Obj = amazonS3.getObject(new GetObjectRequest(ontologyBucketName, key));
        try (BufferedInputStream in = new BufferedInputStream(s3Obj.getObjectContent())) {
            return (new ObjectMapper()).readValue(in, OntologyStoreObject.class);
        }
    }

}
