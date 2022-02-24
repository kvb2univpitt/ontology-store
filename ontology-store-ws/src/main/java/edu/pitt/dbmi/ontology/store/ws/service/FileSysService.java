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

    public List<String> getResourceFileContentByLines(Path file) throws IOException {
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

        return list;
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

    public boolean hasStartedDownload(String productFolder) {
        return Files.exists(getDownloadStartedIndicatorFile(productFolder));
    }

    public boolean hasFailedDownload(String productFolder) {
        return Files.exists(getDownloadFailedIndicatorFile(productFolder));
    }

    public boolean hasFinshedDownload(String productFolder) {
        return Files.exists(getDownloadFinishedIndicatorFile(productFolder));
    }

    public boolean hasStartedInstall(String productFolder) {
        return Files.exists(getInstallStartedIndicatorFile(productFolder));
    }

    public boolean hasFailedInstall(String productFolder) {
        return Files.exists(getInstallFailedIndicatorFile(productFolder));
    }

    public boolean hasFinshedInstall(String productFolder) {
        return Files.exists(getInstallFinishedIndicatorFile(productFolder));
    }

    public boolean hasNetworkFiles(String productFolder) {
        Path networkDir = getNetworkDirectory(productFolder);

        return Files.exists(networkDir) && !listFiles(networkDir).isEmpty();
    }

    public Path getProductDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder);
    }

    public Path getOntologyDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "ontology");
    }

    public Path getNetworkDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "network_files");
    }

    public Path getDownloadStartedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.started");
    }

    public Path getDownloadFailedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.failed");
    }

    public Path getDownloadFinishedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "download.finished");
    }

    public Path getInstallStartedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.started");
    }

    public Path getInstallFailedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.failed");
    }

    public Path getInstallFinishedIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "install.finished");
    }

    public boolean createDownloadStartedIndicatorFile(String productFolder) {
        deleteFile(getDownloadFailedIndicatorFile(productFolder));
        deleteFile(getDownloadFinishedIndicatorFile(productFolder));

        return createFile(getDownloadStartedIndicatorFile(productFolder));
    }

    public boolean createDownloadFailedIndicatorFile(String productFolder) {
        deleteFile(getDownloadFinishedIndicatorFile(productFolder));
        deleteFile(getDownloadStartedIndicatorFile(productFolder));

        return createFile(getDownloadFailedIndicatorFile(productFolder));
    }

    public boolean createDownloadFinishedIndicatorFile(String productFolder) {
        deleteFile(getDownloadFailedIndicatorFile(productFolder));
        deleteFile(getDownloadStartedIndicatorFile(productFolder));

        return createFile(getDownloadFinishedIndicatorFile(productFolder));
    }

    public boolean createInstallStartedIndicatorFile(String productFolder) {
        deleteFile(getInstallFailedIndicatorFile(productFolder));
        deleteFile(getInstallFinishedIndicatorFile(productFolder));

        return createFile(getInstallStartedIndicatorFile(productFolder));
    }

    public boolean createInstallFailedIndicatorFile(String productFolder) {
        deleteFile(getInstallFinishedIndicatorFile(productFolder));
        deleteFile(getInstallStartedIndicatorFile(productFolder));

        return createFile(getInstallFailedIndicatorFile(productFolder));
    }

    public boolean createInstallFinishedIndicatorFile(String productFolder) {
        deleteFile(getInstallFailedIndicatorFile(productFolder));
        deleteFile(getInstallStartedIndicatorFile(productFolder));

        return createFile(getInstallFinishedIndicatorFile(productFolder));
    }

    public List<Path> listFiles(Path dir) {
        List<Path> files = new LinkedList<>();

        try {
            Files.list(dir)
                    .filter(Files::isRegularFile)
                    .forEach(files::add);
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to list files in directory '%s'.", dir.toString()), exception);
        }

        return files;
    }

    public boolean deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to delete file '%s'.", file.toString()), exception);
        }

        return Files.notExists(file);
    }

    public boolean createFile(Path file) {
        if (Files.notExists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create file '%s'.", file.toString()), exception);
            }
        }

        return Files.exists(file) && Files.isRegularFile(file);
    }

    public boolean createDirectories(Path dir) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create directory '%s'.", dir.toString()), exception);
            }
        }

        return Files.exists(dir) && Files.isDirectory(dir);
    }

}
