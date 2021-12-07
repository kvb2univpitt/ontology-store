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

import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyStoreObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 7, 2021 1:23:48 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyDownloadService {

    private final AmazonS3Service amazonS3Service;
    private final FileSysService fileSysService;

    @Autowired
    public OntologyDownloadService(AmazonS3Service amazonS3Service, FileSysService fileSysService) {
        this.amazonS3Service = amazonS3Service;
        this.fileSysService = fileSysService;
    }

    public void performDownload(List<OntologyProductAction> actions) throws IOException {
        // get a list of selected ontologies to download
        List<OntologyProductAction> listOfDownloads = actions.stream()
                .filter(e -> e.isDownload())
                .collect(Collectors.toList());

        if (!listOfDownloads.isEmpty()) {
            Path parentDir = fileSysService.getLocalDownloadDirectory();
            for (OntologyProductAction action : listOfDownloads) {
                performOntologyProductDownload(action, parentDir);
            }
        }
    }

    private void performOntologyProductDownload(OntologyProductAction action, Path parentDir) throws IOException {
        String key = action.getKey();
        String dir = key.replaceAll(".json", "");
        Path folder = Paths.get(parentDir.toString(), dir);
        if (Files.exists(folder)) {
            throw new IOException("Ontology has already been downloaded.");
        }

        OntologyStoreObject storeObject = amazonS3Service.getOntologyStoreObject(key);
        if (storeObject != null) {
            try {
                fileSysService.createDirectory(folder);
            } catch (IOException exception) {
                throw new IOException(String.format("Unable to create folder %s.", dir));
            }

            downloadFile(storeObject.getSchemes(), folder);
            downloadFile(storeObject.getTableAccess(), folder);

            String[] domainOntologies = storeObject.getListOfDomainOntologies();
            if (domainOntologies.length > 0) {
                Path ontologyFolder = Paths.get(folder.toString(), "ontology");
                try {
                    fileSysService.createDirectory(ontologyFolder);
                } catch (IOException exception) {
                    throw new IOException("Unable to create folder ontology.");
                }

                for (String uri : domainOntologies) {
                    downloadFile(uri, ontologyFolder);
                }
            }
        }
    }

    private static void downloadFile(String uri, Path folder) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(folder.toString(), fileName);
        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IOException(String.format("Unable to download file %s.", fileName));
        }
    }

}
