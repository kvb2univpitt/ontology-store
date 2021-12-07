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
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProduct;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import java.io.BufferedInputStream;
import java.io.IOException;
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

    private final String bucketName;
    private final String keyName;

    private final AmazonS3 amazonS3;

    @Autowired
    public AmazonS3Service(
            @Value("${ontology.aws.s3.bucket.name}") String bucketName,
            @Value("${ontology.aws.s3.key.name}") String keyName,
            AmazonS3 amazonS3) {
        this.bucketName = bucketName;
        this.keyName = keyName;
        this.amazonS3 = amazonS3;
    }

    public List<OntologyProduct> getProducts() {
        List<OntologyProduct> products = new LinkedList<>();

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(keyName + "/")
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
                        OntologyProduct product = new OntologyProduct(objSummary.getKey());
                        product.setTitle(obj.getProductTitle());
                        product.setVersion(obj.getProductVersion());
                        product.setOwner(obj.getProductOwner());
                        product.setType(obj.getProductType());
                        product.setIncludeNetworkPackage(obj.getIncludeNetworkPackage().equals("Y"));
                        product.setTerminologies(obj.getTerminologies());
                        products.add(product);
                    } catch (IOException exception) {
                        exception.printStackTrace(System.err);
                    }
                });

        return products;
    }

    public List<OntologyStoreObject> listOntologyStoreObject() {
        List<OntologyStoreObject> ontologyStoreObjects = new LinkedList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(keyName + "/")
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
        S3Object s3Obj = amazonS3.getObject(new GetObjectRequest(bucketName, key));
        try (BufferedInputStream in = new BufferedInputStream(s3Obj.getObjectContent())) {
            return (new ObjectMapper()).readValue(in, OntologyStoreObject.class);
        }
    }

}
