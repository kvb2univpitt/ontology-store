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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 7, 2021 2:39:01 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class FileSysService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSysService.class);

    @Value("${ontology.dir.download}")
    private String downloadDirectory;

    public FileSysService() {
    }

    public Path getOntologyDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "ontology");
    }

    public Path getProductDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder);
    }

    public boolean createFile(Path file) {
        if (Files.notExists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create file '%s'.", file.toString()), exception);

                return false;
            }
        }

        return true;
    }

    public boolean createDirectory(Path dir) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create directory '%s'.", dir.toString()), exception);

                return false;
            }
        }

        return true;
    }

}
