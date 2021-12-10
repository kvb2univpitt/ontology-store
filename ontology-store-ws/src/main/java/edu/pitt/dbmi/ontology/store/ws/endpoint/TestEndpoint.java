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

import edu.pitt.dbmi.ontology.store.ws.service.SchemesTableService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Dec 8, 2021 2:05:09 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Path("test")
public class TestEndpoint {

    private final SchemesTableService schemesTableService;

    @Autowired
    public TestEndpoint(SchemesTableService schemesTableService) {
        this.schemesTableService = schemesTableService;
    }

    @GET
    @Path("products")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts() throws Exception {
        return Response.ok(schemesTableService.getAll()).build();
    }

}
