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

import java.nio.file.Path;

/**
 * Represents files and parentDirectories located in the OntologyStore download
 * parentDirectory.
 *
 * Feb 3, 2026 3:08:00 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class SystemFiles {

    /**
     * Directories.
     */
    public static final String METADATA = "metadata";
    public static final String CRC = "crc";
    public static final String NETWORK_FILES = "network_files";
    public static final String TABLE_ACCESS = "table_access";

    /**
     * Download status indicator files.
     */
    public static final String DOWNLOAD_STARTED = "download.started";
    public static final String DOWNLOAD_FAILED = "download.failed";
    public static final String DOWNLOAD_FINISHED = "download.finished";
    public static final String DOWNLOAD_PENDING = "download.pending";

    /**
     * Install status indicator files.
     */
    public static final String INSTALL_STARTED = "install.started";
    public static final String INSTALL_FAILED = "install.failed";
    public static final String INSTALL_FINISHED = "install.finished";
    public static final String INSTALL_PENDING = "install.pending";

    /**
     * Disabled status indicator files.
     */
    public static final String DISABLED = "disabled";

    private SystemFiles() {
    }

    public static final Path getMetadataDirectory(Path parentDir) {
        return parentDir.resolve(METADATA);
    }

    public static final Path getCRCDirectory(Path parentDir) {
        return parentDir.resolve(CRC);
    }

    public static final Path getNetworkDirectory(Path parentDir) {
        return parentDir.resolve(NETWORK_FILES);
    }

    public static final Path getTableAccessDirectory(Path parentDir) {
        return parentDir.resolve(TABLE_ACCESS);
    }

    public static final Path getDownloadStartedFile(Path parentDir) {
        return parentDir.resolve(DOWNLOAD_STARTED);
    }

    public static final Path getDownloadFailedFile(Path parentDir) {
        return parentDir.resolve(DOWNLOAD_FAILED);
    }

    public static final Path getDownloadFinishedFile(Path parentDir) {
        return parentDir.resolve(DOWNLOAD_FINISHED);
    }

    public static final Path getDownloadPendingFile(Path parentDir) {
        return parentDir.resolve(DOWNLOAD_PENDING);
    }

    public static final Path getInstallStartedFile(Path parentDir) {
        return parentDir.resolve(INSTALL_STARTED);
    }

    public static final Path getInstallFailedFile(Path parentDir) {
        return parentDir.resolve(INSTALL_FAILED);
    }

    public static final Path getInstallFinishedFile(Path parentDir) {
        return parentDir.resolve(INSTALL_FINISHED);
    }

    public static final Path getInstallPendingFile(Path parentDir) {
        return parentDir.resolve(INSTALL_PENDING);
    }

    public static final Path getDisabledFile(Path parentDir) {
        return parentDir.resolve(DISABLED);
    }

}
