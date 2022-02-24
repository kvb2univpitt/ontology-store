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
package edu.pitt.dbmi.ontology.store.ws.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 *
 * Feb 24, 2022 10:56:01 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Provider
public class ProjectRequiredHeaderFilter implements ContainerRequestFilter {

    public static final String I2B2_PROJECT_HAEDER = "X-I2B2-Project";

    @Override
    public void filter(ContainerRequestContext reqCtx) throws IOException {
        if (reqCtx.getUriInfo().getPath().equals("action")) {
            String project = reqCtx.getHeaderString(I2B2_PROJECT_HAEDER);
            if (project == null) {
                String msg = String.format("Header '%s' is required.", I2B2_PROJECT_HAEDER);
                Response res = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();

                reqCtx.abortWith(res);
            } else {
                project = project.trim();

                if (project.isEmpty() || project.equals("null")) {
                    String msg = String.format("Value is required for header '%s'.", I2B2_PROJECT_HAEDER);
                    Response res = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();

                    reqCtx.abortWith(res);
                }
            }
        }
    }

}
