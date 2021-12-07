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
package edu.pitt.dbmi.ontology.store.ws.endpoint;

import edu.pitt.dbmi.ontology.store.ws.service.AmazonS3Service;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * List of products stored on Amazon S3 as JSON files.
 *
 * Dec 7, 2021 12:07:40 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Path("products")
public class OntologyProductEndpoint {

    private final AmazonS3Service amazonS3Service;

    @Autowired
    public OntologyProductEndpoint(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() throws Exception {
        return Response.ok(amazonS3Service.getProducts()).build();
    }

}
