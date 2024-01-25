/*
 * Copyright (C) 2024 University of Pittsburgh.
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

import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 6, 2023 12:13:19 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class FileSysService {

    private static final Log LOGGER = LogFactory.getLog(FileSysService.class);

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

    public boolean hasDirectory(String productFolder) {
        return Files.exists(Paths.get(downloadDirectory, productFolder));
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

    public boolean hasOntologyDisabled(String productFolder) {
        return Files.exists(getOntologyDisabledIndicatorFile(productFolder));
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

    public Path getOntologyDisabledIndicatorFile(String productFolder) {
        return Paths.get(downloadDirectory, productFolder, "ontology.disabled");
    }

    public boolean createDownloadStartedIndicatorFile(String productFolder) {
        deleteFile(getDownloadFailedIndicatorFile(productFolder));
        deleteFile(getDownloadFinishedIndicatorFile(productFolder));

        return createFile(getDownloadStartedIndicatorFile(productFolder));
    }

    public boolean createDownloadFinishedIndicatorFile(String productFolder) {
        deleteFile(getDownloadFailedIndicatorFile(productFolder));
        deleteFile(getDownloadStartedIndicatorFile(productFolder));

        return createFile(getDownloadFinishedIndicatorFile(productFolder));
    }

    public boolean createDownloadFailedIndicatorFile(String productFolder, String reason) {
        deleteFile(getDownloadFinishedIndicatorFile(productFolder));
        deleteFile(getDownloadStartedIndicatorFile(productFolder));

        Path file = getDownloadFailedIndicatorFile(productFolder);
        if (reason == null || reason.trim().isEmpty()) {
            return createFile(file);
        } else {
            return createFileAndWriteMessage(file, reason);
        }
    }

    public boolean createInstallStartedIndicatorFile(String productFolder) {
        deleteFile(getInstallFailedIndicatorFile(productFolder));
        deleteFile(getInstallFinishedIndicatorFile(productFolder));

        return createFile(getInstallStartedIndicatorFile(productFolder));
    }

    public boolean createInstallFinishedIndicatorFile(String productFolder) {
        deleteFile(getInstallFailedIndicatorFile(productFolder));
        deleteFile(getInstallStartedIndicatorFile(productFolder));

        return createFile(getInstallFinishedIndicatorFile(productFolder));
    }

    public boolean createInstallFailedIndicatorFile(String productFolder, String reason) {
        deleteFile(getInstallFinishedIndicatorFile(productFolder));
        deleteFile(getInstallStartedIndicatorFile(productFolder));

        Path file = getInstallFailedIndicatorFile(productFolder);
        if (reason == null || reason.trim().isEmpty()) {
            return createFile(file);
        } else {
            return createFileAndWriteMessage(file, reason);
        }
    }

    public boolean createOntologyDisabledIndicatorFile(String productFolder) {
        return createFile(getOntologyDisabledIndicatorFile(productFolder));
    }

    public boolean removeOntologyDisabledIndicatorFile(String productFolder) {
        return deleteFile(getOntologyDisabledIndicatorFile(productFolder));
    }

    public String getFailedDownloadMessage(String productFolder) {
        return readFromFile(getDownloadFailedIndicatorFile(productFolder), "Download previously failed.");
    }

    public String getFailedInstallMessage(String productFolder) {
        return readFromFile(getInstallFailedIndicatorFile(productFolder), "Install previously failed.");
    }

    public List<Path> listFiles(Path dir) {
        List<Path> files = new LinkedList<>();

        try {
            Files.list(dir)
                    .filter(Files::isRegularFile)
                    .forEach(files::add);
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to list files in directory %s.", dir.toString()), exception);
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

    private boolean createFileAndWriteMessage(Path file, String message) {
        if (message == null || message.trim().isEmpty()) {
            return true;
        } else {
            try {
                Files.write(file, message.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException exception) {
                LOGGER.error(String.format("Unable to write message to file: '%s'.", file.toString()), exception);

                return false;
            }
        }

        return true;
    }

    public String readFromFile(Path file, String defaultMessage) {
        try {
            return new String(Files.readAllBytes(file));
        } catch (IOException exception) {
            LOGGER.error(String.format("Unable to read file: %s.", file.toString()), exception);
        }

        return defaultMessage;
    }

    public void downloadFile(String uri, Path productDir) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(productDir.toString(), fileName);

        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String getSha256Checksum(String uri, Path productDir) {
        StringBuilder hexValue = new StringBuilder();

        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = Paths.get(productDir.toString(), fileName);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(Files.newInputStream(file)), md)) {
                //empty loop to clear the data
                while (dis.read() != -1);

                md = dis.getMessageDigest();

                // byte to hex
                for (byte b : md.digest()) {
                    hexValue.append(String.format("%02x", b));
                }
            }
        } catch (NoSuchAlgorithmException | IOException exception) {
            LOGGER.error(String.format("Unable to create the SHA-256 checksum for file: %s.", file.toString()), exception);
        }

        return hexValue.toString();
    }

    public boolean isProductFileExists(ProductItem productItem) {
        return Files.exists(getProductFile(productItem));
    }

    public Path getProductFile(ProductItem productItem) {
        String productFolder = productItem.getId();
        String fileURI = productItem.getFile();
        String fileName = fileURI.substring(fileURI.lastIndexOf("/") + 1, fileURI.length());

        Path productDir = getProductDirectory(productFolder);

        return Paths.get(productDir.toString(), fileName);
    }

}
