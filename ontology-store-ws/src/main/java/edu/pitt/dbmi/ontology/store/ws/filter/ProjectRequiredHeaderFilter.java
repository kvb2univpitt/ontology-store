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

import edu.pitt.dbmi.ontology.store.ws.db.PmDBAccess;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Feb 24, 2022 10:56:01 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Provider
public class ProjectRequiredHeaderFilter implements ContainerRequestFilter {

    private static final Pattern SPACE_DELIM = Pattern.compile("\\s+");

    private static final String ACTION_PATH = "action";

    private static final String BASIC_AUTH_HAEDER = "Authorization";
    public static final String I2B2_PROJECT_HAEDER = "X-I2B2-Project";
    public static final String I2B2_DOMAIN_HAEDER = "X-I2B2-Domain";

    private final PmDBAccess pmDBAccess;

    @Autowired
    public ProjectRequiredHeaderFilter(PmDBAccess pmDBAccess) {
        this.pmDBAccess = pmDBAccess;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        if (ACTION_PATH.equals(path) && hasRequiredHeaders(requestContext)) {
            String basicAuth = getHeaderValue(requestContext, BASIC_AUTH_HAEDER);
            String i2b2Project = getHeaderValue(requestContext, I2B2_PROJECT_HAEDER);
            String i2b2Domain = getHeaderValue(requestContext, I2B2_DOMAIN_HAEDER);

            String auth = new String(Base64.getDecoder().decode(SPACE_DELIM.split(basicAuth)[1].trim()));
            String[] fields = auth.split(":");
            if (fields.length == 3) {
                String userId = fields[0];
                String sessionId = fields[2].replaceAll("</password>", "");

                try {
                    if (pmDBAccess.hasExpiredSession(userId, sessionId) || !pmDBAccess.isAdmin(userId)) {
                        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                    }
                } catch (SQLException exception) {
                    requestContext
                            .abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .entity("Unable to authenticate user.")
                                    .build());
                }
            } else {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }

    private boolean hasRequiredHeaders(ContainerRequestContext requestContext) {
        if (requestContext.getHeaderString(I2B2_PROJECT_HAEDER) == null) {
            requestContext
                    .abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity(String.format("Required Header: %s.", I2B2_PROJECT_HAEDER))
                            .build());
            return false;
        } else if (requestContext.getHeaderString(I2B2_DOMAIN_HAEDER) == null) {
            requestContext
                    .abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity(String.format("Required Header: %s.", I2B2_DOMAIN_HAEDER))
                            .build());
            return false;
        } else if (requestContext.getHeaderString(BASIC_AUTH_HAEDER) == null) {
            requestContext
                    .abortWith(Response.status(Response.Status.BAD_REQUEST)
                            .entity(String.format("Required Header: %s.", BASIC_AUTH_HAEDER))
                            .build());
            return false;
        } else {
            return true;
        }
    }

    private String getHeaderValue(ContainerRequestContext requestContext, String header) {
        String value = requestContext.getHeaderString(header);

        return (value == null) ? "" : value.trim();
    }

}
