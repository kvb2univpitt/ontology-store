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

import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import edu.pitt.dbmi.ontology.store.ws.service.OntologyDownloadService;
import edu.pitt.dbmi.ontology.store.ws.service.OntologyInstallService;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Dec 7, 2021 1:16:40 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Path("action")
public class OntologyActionEndpoint {

    private final OntologyDownloadService downloadService;
    private final OntologyInstallService installService;

    @Autowired
    public OntologyActionEndpoint(OntologyDownloadService downloadService, OntologyInstallService installService) {
        this.downloadService = downloadService;
        this.installService = installService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response performAction(List<OntologyProductAction> productActions) {
        List<ActionSummary> summaries = new LinkedList<>();
        downloadService.performDownload(productActions, summaries);
        installService.performInstallation(productActions, summaries);

        return Response.ok(summaries).build();
    }

}
