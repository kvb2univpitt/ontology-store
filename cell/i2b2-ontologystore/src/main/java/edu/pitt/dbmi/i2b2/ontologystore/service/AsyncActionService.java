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

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 9, 2026 3:15:50 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class AsyncActionService extends AbstractOntologyService {

    private final OntologyDownloadService ontologyDownloadService;
    private final OntologyInstallService ontologyInstallService;

    @Autowired
    public AsyncActionService(OntologyDownloadService ontologyDownloadService, OntologyInstallService ontologyInstallService) {
        this.ontologyDownloadService = ontologyDownloadService;
        this.ontologyInstallService = ontologyInstallService;
    }

    @Async
    public CompletableFuture<List<ActionSummaryType>> performActions(
            String projectId,
            String downloadDirectory,
            String productListUrl,
            List<ProductActionType> actions) {
        List<ActionSummaryType> summaries = new LinkedList<>();

        ontologyDownloadService.performDownload(downloadDirectory, productListUrl, actions, summaries);
        ontologyInstallService.performInstallation(projectId, downloadDirectory, productListUrl, actions, summaries);

        return CompletableFuture.completedFuture(summaries);
    }

}
