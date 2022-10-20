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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 *
 * Oct 19, 2022 3:37:13 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class FileSysService {

    private static final Log LOGGER = LogFactory.getLog(FileSysService.class);

    public static final String SCHEMES_FILE = "SCHEMES.tsv";
    public static final String QT_BREAKDOWN_PATH_FILE = "QT_BREAKDOWN_PATH.tsv";

    public static final Pattern TAB_DELIM = Pattern.compile("\t");

    private final String downloadDirectory;
    private final ResourcePatternResolver resourcePatternResolver;

    public FileSysService(String downloadDirectory, ResourcePatternResolver resourcePatternResolver) {
        this.downloadDirectory = Paths.get(downloadDirectory, "products").toString();
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public String getNewDownloadFinishedIndicatorFile(String productFolder) {
        return getDownloadFinishedIndicatorFile(productFolder).toString();
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

        return list.stream().collect(Collectors.joining());
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

    public Path getSchemesFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, SCHEMES_FILE);
    }

    public Path getQtBreakdownPathFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, QT_BREAKDOWN_PATH_FILE);
    }

    public List<Path> getMetadata(String productFolder) {
        List<Path> ontologies = new LinkedList<>();
        try {
            Files.list(getMetadataDirectory(productFolder))
                    .filter(Files::isRegularFile)
                    .forEach(ontologies::add);
        } catch (IOException exception) {
            LOGGER.error("Unable to get ontology files.", exception);
        }

        return ontologies;
    }

    public List<Path> getCrcData(String productFolder) {
        List<Path> ontologies = new LinkedList<>();
        try {
            Files.list(getCRCDirectory(productFolder))
                    .filter(Files::isRegularFile)
                    .forEach(ontologies::add);
        } catch (IOException exception) {
            LOGGER.error("Unable to get ontology files.", exception);
        }

        return ontologies;
    }

    public List<Path> getTableAccess(String productFolder) {
        List<Path> ontologies = new LinkedList<>();
        try {
            Files.list(getTableAccessDirectory(productFolder))
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

    public Path getMetadataDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "metadata");
    }

    public Path getCRCDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "crc");
    }

    public Path getNetworkDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "network_files");
    }

    public Path getTableAccessDirectory(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "table_access");
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

    public boolean createDirectory(Path dir) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to create directory '%s'.", dir.toString()), exception);
            }
        }

        return Files.exists(dir) && Files.isDirectory(dir);
    }

    public boolean createDirectories(Path dir, Path... dirs) {
        if (dirs == null || dirs.length == 0) {
            return false;
        }

        boolean success = createDirectory(dir);
        for (Path directory : dirs) {
            success = createDirectory(directory) && success;
        }

        return success;
    }

}
