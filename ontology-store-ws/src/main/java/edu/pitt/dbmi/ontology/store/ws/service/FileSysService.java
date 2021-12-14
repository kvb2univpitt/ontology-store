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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

    public static final Pattern TAB_DELIM = Pattern.compile("\t");

    @Value("${ontology.dir.download}")
    private String downloadDirectory;

    public FileSysService() {
    }

    public List<String> getHeaders(Path file) throws IOException {
        Optional<String> header = Files.lines(file).findFirst();
        if (header.isPresent()) {
            return Arrays.stream(TAB_DELIM.split(header.get()))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public Path getTableAccessFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "TABLE_ACCESS.tsv");
    }

    public Path getSchemesFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "SCHEMES.tsv");
    }

    public Path getInstallStartIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "installation.started");
    }

    public Path getInstallFailedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "installation.failed");
    }

    public Path getInstallFinishedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "installation.finished");
    }

    public void createInstallStartIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getInstallFailedIndicatorFile(productFolder));
            Files.deleteIfExists(getInstallFinishedIndicatorFile(productFolder));

            createFile(getInstallStartIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create start indicator file.", exception);
        }
    }

    public void createInstallFailedIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getInstallStartIndicatorFile(productFolder));
            Files.deleteIfExists(getInstallFinishedIndicatorFile(productFolder));

            createFile(getInstallFailedIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create failed indicator file.", exception);
        }
    }

    public void createInstallFinishedIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getInstallFailedIndicatorFile(productFolder));
            Files.deleteIfExists(getInstallStartIndicatorFile(productFolder));

            createFile(getInstallFinishedIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create finish indicator file.", exception);
        }
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

    public boolean createDirectories(Path dir) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create directory '%s'.", dir.toString()), exception);

                return false;
            }
        }

        return true;
    }

}
