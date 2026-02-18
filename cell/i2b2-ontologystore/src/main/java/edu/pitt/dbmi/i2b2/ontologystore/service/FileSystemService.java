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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 18, 2026 1:42:44 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class FileSystemService {

    private static final Log LOGGER = LogFactory.getLog(FileSystemService.class);

    public static final Pattern TAB_DELIM = Pattern.compile("\t");

    private final ResourcePatternResolver resourcePatternResolver;

    @Autowired
    public FileSystemService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
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

    public String getResourceFileContents(Path file) throws IOException {
        return getResourceFileContentByLines(file).stream().collect(Collectors.joining());
    }

    /**
     * Check to see if directory exists.
     *
     * @param dir to check
     * @return true if dir exists and is a directory
     */
    public boolean hasDirectory(Path dir) {
        return Files.exists(dir) && Files.isDirectory(dir);
    }

    /**
     * Check to see if file exists.
     *
     * @param file to check
     * @return true if file exists and is a file
     */
    public boolean hasFile(Path file) {
        return Files.exists(file) && Files.isRegularFile(file);
    }

    public List<Path> listDirectoryFiles(Path dir) {
        List<Path> files = new LinkedList<>();

        try {
            Files.list(dir)
                    .filter(Files::isRegularFile)
                    .forEach(files::add);
        } catch (IOException exception) {
            LOGGER.error(String.format("Failed to list files in directory '%s'.", dir.toString()), exception);
        }

        return files;
    }

    public boolean createFile(Path file) {
        if (Files.notExists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException exception) {
                LOGGER.error(String.format("Failed to create file '%s'.", file.toString()), exception);
            }
        }

        return Files.exists(file) && Files.isRegularFile(file);
    }

    public boolean moveFile(Path source, Path target) {
        if (Files.exists(source)) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                LOGGER.error(String.format("Failed to move/rename file '%s'.", source.toString()), exception);
            }
        }

        return Files.exists(target);
    }

    public boolean deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            LOGGER.error(String.format("Failed to delete file '%s'.", file.toString()), exception);
        }

        return Files.notExists(file);
    }

    public boolean createDirectoryIfNotExists(Path dir) {
        if (Files.notExists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException exception) {
                LOGGER.error(String.format("Failed to create directory '%s'.", dir.toString()), exception);
            }
        }

        return Files.exists(dir) && Files.isDirectory(dir);
    }

    public boolean createDirectories(Path dir, Path... dirs) {
        if (dirs == null || dirs.length == 0) {
            return false;
        }

        boolean success = createDirectoryIfNotExists(dir);
        for (Path directory : dirs) {
            success = createDirectoryIfNotExists(directory) && success;
        }

        return success;
    }

    public boolean createFileWithContent(Path file, String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        } else {
            try {
                Files.write(file, content.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException exception) {
                LOGGER.error(String.format("Failed to write message to file: '%s'.", file.toString()), exception);

                return false;
            }
        }

        return true;
    }

    public String readContentFromFile(Path file, String defaultContent) {
        try {
            return new String(Files.readAllBytes(file));
        } catch (IOException exception) {
            LOGGER.error(String.format("Failed to read file: %s.", file.toString()), exception);
        }

        return defaultContent;
    }

    public void downloadFile(String uri, Path parentDir) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path fileToDownload = parentDir.resolve(fileName);
        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, fileToDownload, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public String getSha256Checksum(Path parentDir, String uri) {
        StringBuilder hexValue = new StringBuilder();

        String fileName = uri.substring(uri.lastIndexOf("/") + 1);
        Path file = parentDir.resolve(fileName);
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
            LOGGER.error(String.format("Failed to create the SHA-256 checksum for file: %s.", file.toString()), exception);
        }

        return hexValue.toString();
    }

}
