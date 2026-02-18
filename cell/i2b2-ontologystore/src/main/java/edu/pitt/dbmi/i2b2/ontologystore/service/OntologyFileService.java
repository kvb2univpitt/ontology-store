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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.TerminologiesType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 5, 2023 6:01:17 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyFileService {

    private static final Log LOGGER = LogFactory.getLog(OntologyFileService.class);

    private final ObjectMapper objMapper = new ObjectMapper();

    private final FileSystemService fileSystemService;

    @Autowired
    public OntologyFileService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    public List<ProductType> getAvailableProducts(String downloadDirectory, String productListUrl) {
        List<ProductType> productDisplays = new LinkedList<>();

        try {
            getProducts(productListUrl).stream()
                    .map(productItem -> toProductTypes(downloadDirectory, productItem))
                    .forEach(productDisplays::add);
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return productDisplays;
    }

    private ProductType toProductTypes(String downloadDirectory, ProductItem productItem) {
        ProductType productType = new ProductType();
        productType.setId(productItem.getId());
        productType.setTitle(productItem.getTitle());
        productType.setVersion(productItem.getVersion());
        productType.setOwner(productItem.getOwner());
        productType.setType(productItem.getType());
        productType.setIncludeNetworkPackage(hasNetworkFiles(productItem.getNetworkFiles()));

        TerminologiesType terminologies = new TerminologiesType();
        terminologies.getTerminology().addAll(Arrays.asList(productItem.getTerminologies()));
        productType.setTerminologies(terminologies);

        getStatus(downloadDirectory, productType, productItem);

        return productType;
    }

    private boolean hasNetworkFiles(String[] networkFiles) {
        return !(networkFiles == null || networkFiles.length == 0);
    }

    private void getStatus(String downloadDirectory, ProductType product, ProductItem productItem) {
        String productFolder = product.getId();
        Path productDir = Paths.get(downloadDirectory, productFolder);
        Path productFile = getProductFile(productDir, productItem);
        if (hasDirectory(productDir)) {
            if (isDownloadCompletelyFinshed(productDir, productFile)) {
                product.setDownloaded(true);
                product.setIncludeNetworkPackage(hasNetworkFileDirectory(productDir));

                if (isInstallFinshed(productDir)) {
                    product.setInstalled(true);

                    if (isDisabled(productDir)) {
                        product.setDisabled(true);
                    }
                } else if (isInstallFailed(productDir)) {
                    product.setFailed(true);
                    product.setStatusDetail(getInstallFailedMessage(productDir));
                } else if (isInstallStarted(productDir)) {
                    product.setStarted(true);
                }
            } else if (isDownloadFailed(productDir)) {
                product.setFailed(true);
                product.setStatusDetail(getDownloadFailedMessage(productDir));
            } else if (isDownloadStarted(productDir)) {
                product.setStarted(true);
            }
        }
    }

    public Map<String, ProductItem> getProductItems(String productListUrl) {
        try {
            return getProducts(productListUrl).stream()
                    .collect(Collectors.toMap(e -> e.getId(), Function.identity()));
        } catch (IOException exception) {
            return Collections.EMPTY_MAP;
        }
    }

    private List<ProductItem> getProducts(String productListUrl) throws IOException {
        List<ProductItem> productItems = new LinkedList<>();
        try {
            ProductList productList = objMapper.readValue(new URL(productListUrl), ProductList.class);
            if (productList != null) {
                productItems.addAll(productList.getProducts());
            }
        } catch (IOException exception) {
            LOGGER.error("", exception);
        }

        return productItems;
    }

    public Path getProductFile(Path productDir, ProductItem productItem) {
        String fileURI = productItem.getFile();
        String fileName = fileURI.substring(fileURI.lastIndexOf("/") + 1, fileURI.length());

        return productDir.resolve(fileName);
    }

    public void downloadFile(String uri, Path productDir) throws IOException {
        String fileName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        Path file = productDir.resolve(fileName);

        try (InputStream inputStream = URI.create(uri).toURL().openStream()) {
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public boolean createDirectory(Path productDir) {
        return fileSystemService.createDirectoryIfNotExists(productDir);
    }

    public String getDownloadFailedMessage(Path productDir) {
        Path file = SystemFiles.getDownloadFailedFile(productDir);
        String defaultErrorMessage = "Download previously failed.";

        return fileSystemService.readContentFromFile(file, defaultErrorMessage);
    }

    public String getInstallFailedMessage(Path productDir) {
        Path file = SystemFiles.getInstallFailedFile(productDir);
        String defaultErrorMessage = "Install previously failed.";

        return fileSystemService.readContentFromFile(file, defaultErrorMessage);
    }

    public boolean hasNetworkFileDirectory(Path productDir) {
        return fileSystemService.hasDirectory(SystemFiles.getNetworkDirectory(productDir));
    }

    public boolean hasDirectory(Path productDir) {
        return fileSystemService.hasDirectory(productDir);
    }

    public boolean isDownloadFinshed(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getDownloadFinishedFile(productDir));
    }

    public boolean isDownloadCompletelyFinshed(Path productDir, Path productFile) {
        return fileSystemService.hasFile(SystemFiles.getDownloadFinishedFile(productDir))
                && fileSystemService.hasFile(productFile);
    }

    public boolean isDownloadFailed(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getDownloadFailedFile(productDir));
    }

    public boolean isDownloadStarted(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getDownloadStartedFile(productDir));
    }

    public boolean isInstallFinshed(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getInstallFinishedFile(productDir));
    }

    public boolean isInstallFailed(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getInstallFailedFile(productDir));
    }

    public boolean isInstallStarted(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getInstallStartedFile(productDir));
    }

    public boolean isDisabled(Path productDir) {
        return fileSystemService.hasFile(SystemFiles.getDisabledFile(productDir));
    }

    public boolean setEnabled(Path productDir) {
        return fileSystemService.deleteFile(SystemFiles.getDisabledFile(productDir));
    }

    public boolean setDisabled(Path productDir) {
        return fileSystemService.createFile(SystemFiles.getDisabledFile(productDir));
    }

    public String createSha256Checksum(Path parentDir, String uri) {
        return fileSystemService.getSha256Checksum(parentDir, uri);
    }

    public boolean setDownloadFinished(Path productDir) {
        Path downloadFinishedFile = SystemFiles.getDownloadFinishedFile(productDir);
        if (isDownloadStarted(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getDownloadStartedFile(productDir), downloadFinishedFile);
        } else if (isDownloadFailed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getDownloadFailedFile(productDir), downloadFinishedFile);
        } else {
            return fileSystemService.createFile(downloadFinishedFile);
        }
    }

    public boolean setDownloadFailed(Path productDir, String errorMsg) {
        Path downloadFailedFile = SystemFiles.getDownloadFailedFile(productDir);
        boolean hasFile;
        if (isDownloadStarted(productDir)) {
            hasFile = fileSystemService.moveFile(SystemFiles.getDownloadStartedFile(productDir), downloadFailedFile);
        } else if (isDownloadFinshed(productDir)) {
            hasFile = fileSystemService.moveFile(SystemFiles.getDownloadFinishedFile(productDir), downloadFailedFile);
        } else {
            hasFile = fileSystemService.createFile(downloadFailedFile);
        }

        return hasFile ? fileSystemService.createFileWithContent(downloadFailedFile, errorMsg) : false;
    }

    public boolean setDownloadStarted(Path productDir) {
        Path downloadStartedFile = SystemFiles.getDownloadStartedFile(productDir);
        if (isDownloadFailed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getDownloadFailedFile(productDir), downloadStartedFile);
        } else if (isDownloadFinshed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getDownloadFinishedFile(productDir), downloadStartedFile);
        } else {
            return fileSystemService.createFile(downloadStartedFile);
        }
    }

    public boolean setInstallFinished(Path productDir) {
        Path installFinishedFile = SystemFiles.getInstallFinishedFile(productDir);
        if (isInstallStarted(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getInstallStartedFile(productDir), installFinishedFile);
        } else if (isInstallFailed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getInstallFailedFile(productDir), installFinishedFile);
        } else {
            return fileSystemService.createFile(installFinishedFile);
        }
    }

    public boolean setInstallFailed(Path productDir, String errorMsg) {
        Path installFailedFile = SystemFiles.getInstallFailedFile(productDir);
        boolean hasFile;
        if (isInstallStarted(productDir)) {
            hasFile = fileSystemService.moveFile(SystemFiles.getInstallStartedFile(productDir), installFailedFile);
        } else if (isInstallFinshed(productDir)) {
            hasFile = fileSystemService.moveFile(SystemFiles.getInstallFinishedFile(productDir), installFailedFile);
        } else {
            hasFile = fileSystemService.createFile(installFailedFile);
        }

        return hasFile ? fileSystemService.createFileWithContent(installFailedFile, errorMsg) : false;
    }

    public boolean setInstallStarted(Path productDir) {
        Path installStartedFile = SystemFiles.getInstallStartedFile(productDir);
        if (isInstallFailed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getInstallFailedFile(productDir), installStartedFile);
        } else if (isInstallFinshed(productDir)) {
            return fileSystemService.moveFile(SystemFiles.getInstallFinishedFile(productDir), installStartedFile);
        } else {
            return fileSystemService.createFile(installStartedFile);
        }
    }

}
