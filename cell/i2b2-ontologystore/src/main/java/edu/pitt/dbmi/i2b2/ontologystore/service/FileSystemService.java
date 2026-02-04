package edu.pitt.dbmi.i2b2.ontologystore.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 *
 * Feb 3, 2026 3:50:18 PM
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

    public String getResourceFileContents(Path file) throws IOException {
        Resource resource = resourcePatternResolver.getResource("classpath:/" + file.toString());

        return Files.readString(Paths.get(resource.getURI()));
    }

    public String getResourceFileContents(Path file, String defaultMessage) {
        try {
            return getResourceFileContents(file);
        } catch (IOException exception) {
            LOGGER.error(String.format("Failed to read file: %s.", file.toString()), exception);
        }

        return defaultMessage;
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
        if (Files.exists(target)) {
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
            byte[] msg = Files.readAllBytes(file);
            if (msg.length > 0) {
                return new String(msg);
            }
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
