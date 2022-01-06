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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
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

    private static final String TABLE_ACCESS_FILE = "TABLE_ACCESS.tsv";
    private static final String SCHEMES_FILE = "SCHEMES.tsv";

    public static final Pattern TAB_DELIM = Pattern.compile("\t");

    private final String downloadDirectory;
    private final ResourcePatternResolver resourcePatternResolver;

    @Autowired
    public FileSysService(
            @Value("${ontology.dir.download}") String downloadDirectory,
            ResourcePatternResolver resourcePatternResolver) {
        this.downloadDirectory = downloadDirectory;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public String getResourceFileContents(Path file) throws IOException {
        List<String> list = new LinkedList<>();

        Resource resource = resourcePatternResolver.getResource("classpath:/" + file.toString());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }
        }

        return list.stream()
                .collect(Collectors.joining());
    }

    /**
     * Get the file header (the first line of the file).
     *
     * @param file
     * @return
     * @throws IOException
     */
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
        return Paths.get(downloadDirectory, productFolder, TABLE_ACCESS_FILE);
    }

    public Path getSchemesFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, SCHEMES_FILE);
    }

    public void createStartedDownloadIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getFailedDownloadIndicatorFile(productFolder));
            Files.deleteIfExists(getFinishedDownloadIndicatorFile(productFolder));

            createFile(getStartedDownloadIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download started indicator file.", exception);
        }
    }

    public void createFailedDownloadIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getStartedDownloadIndicatorFile(productFolder));
            Files.deleteIfExists(getFinishedDownloadIndicatorFile(productFolder));

            createFile(getFailedDownloadIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download failed indicator file.", exception);
        }
    }

    public void createFinishedDownloadIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getStartedDownloadIndicatorFile(productFolder));
            Files.deleteIfExists(getFailedDownloadIndicatorFile(productFolder));

            createFile(getFinishedDownloadIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download finished indicator file.", exception);
        }
    }

    public void createStartedInstallIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getFailedInstallIndicatorFile(productFolder));
            Files.deleteIfExists(getFinishedInstallIndicatorFile(productFolder));

            createFile(getStartedInstallIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download started indicator file.", exception);
        }
    }

    public void createFailedInstallIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getStartedInstallIndicatorFile(productFolder));
            Files.deleteIfExists(getFinishedInstallIndicatorFile(productFolder));

            createFile(getFailedInstallIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download failed indicator file.", exception);
        }
    }

    public void createFinishedInstallIndicatorFile(String productFolder) {
        try {
            Files.deleteIfExists(getStartedInstallIndicatorFile(productFolder));
            Files.deleteIfExists(getFailedInstallIndicatorFile(productFolder));

            createFile(getFinishedInstallIndicatorFile(productFolder));
        } catch (IOException exception) {
            LOGGER.error("Unable to create download finished indicator file.", exception);
        }
    }

    public Path getStartedDownloadIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.started");
    }

    public Path getFinishedDownloadIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.finished");
    }

    public Path getFailedDownloadIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.failed");
    }

    public Path getStartedInstallIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.started");
    }

    public Path getFinishedInstallIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.finished");
    }

    public Path getFailedInstallIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.failed");
    }

    public Path getOntologyDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "ontology");
    }

    public Path getProductDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder);
    }

    public List<Path> getOntologies(String productFolder) {
        List<Path> ontologies = new LinkedList<>();

        try {
            Files.list(getOntologyDirectory(productFolder))
                    .filter(Files::isRegularFile)
                    .forEach(ontologies::add);
        } catch (IOException exception) {
            LOGGER.error("Unable to get ontology files.", exception);
        }

        return ontologies;
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
                LOGGER.error("", exception);

                return false;
            }
        }

        return true;
    }

}
