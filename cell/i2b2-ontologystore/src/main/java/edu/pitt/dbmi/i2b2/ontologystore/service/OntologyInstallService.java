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

import edu.pitt.dbmi.i2b2.ontologystore.InstallationException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.util.ZipFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 22, 2022 4:24:39 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyInstallService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final HiveDBAccess hiveDBAccess;

    private final MetadataInstallService metadataInstallService;
    private final CrcInstallService crcInstallService;
    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyInstallService(
            HiveDBAccess hiveDBAccess,
            MetadataInstallService metadataInstallService,
            CrcInstallService crcInstallService,
            OntologyFileService ontologyFileService) {
        this.hiveDBAccess = hiveDBAccess;
        this.metadataInstallService = metadataInstallService;
        this.crcInstallService = crcInstallService;
        this.ontologyFileService = ontologyFileService;
    }

    public synchronized void performInstallation(String downloadDirectory, String project, String productListUrl, ProductItem productItem, List<ActionSummaryType> summaries) throws InstallationException {
        String ontJNDIName = hiveDBAccess.getOntDataSourceJNDIName(project);
        String crcJNDIName = hiveDBAccess.getCrcDataSourceJNDIName(project);
        if (ontJNDIName == null || crcJNDIName == null) {
            throw new InstallationException(String.format("No i2b2 datasource(s) associated with project '%s'.", project));
        }

        DataSource ontDataSource = getDataSource(ontJNDIName);
        DataSource crcDataSource = getDataSource(crcJNDIName);
        if (ontDataSource == null || crcDataSource == null) {
            throw new InstallationException(String.format("No i2b2 JNDI datasource(s) found for project '%s'.", project));
        }

        JdbcTemplate ontJdbcTemplate = new JdbcTemplate(ontDataSource);
        JdbcTemplate crcJdbcTemplate = new JdbcTemplate(crcDataSource);

        try {
            install(downloadDirectory, productItem, ontJdbcTemplate, crcJdbcTemplate, summaries);
        } catch (Exception exception) {
            LOGGER.error("", exception);
            summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Metadata Installation Failed."));
            ontologyFileService.setInstallFailed(Paths.get(downloadDirectory, productItem.getId()), exception.getMessage());
        }
    }

    private void install(String downloadDirectory, ProductItem productItem, JdbcTemplate ontJdbcTemplate, JdbcTemplate crcJdbcTemplate, List<ActionSummaryType> summaries) throws InstallationException {
        Path productDir = Paths.get(downloadDirectory, productItem.getId());
        File productFile = ontologyFileService.getProductFile(productDir, productItem).toFile();
        try (ZipFile zipFile = new ZipFile(productFile)) {
            Map<String, ZipEntry> zipEntries = ZipFileUtils.getZipFileEntries(zipFile);

            ZipEntry packageJsonZipEntry = zipEntries.get("package.json");
            PackageFile packageFile = ZipFileUtils.getPackageFile(packageJsonZipEntry, zipFile);

            String rootFolder = new File(packageJsonZipEntry.getName()).getParent();

            try {
                metadataInstallService.createMetadata(packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);
                metadataInstallService.insertIntoSchemesTable(packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);
                metadataInstallService.insertIntoTableAccessTable(packageFile, rootFolder, zipEntries, zipFile, ontJdbcTemplate);

                summaries.add(createActionSummary(productItem, ACTION_TYPE, false, true, "Metadata Installed."));
            } catch (InstallationException exception) {
                summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "Metadata Installation Failed."));

                throw exception;
            }

            try {
                crcInstallService.createConceptDimension(packageFile, rootFolder, zipEntries, zipFile, crcJdbcTemplate);
                crcInstallService.insertIntoQtBreakdownPathTable(packageFile, rootFolder, zipEntries, zipFile, crcJdbcTemplate);

                summaries.add(createActionSummary(productItem, ACTION_TYPE, false, true, "CRC Data Installed."));
            } catch (InstallationException exception) {
                summaries.add(createActionSummary(productItem, ACTION_TYPE, false, false, "CRC Data Installation Failed."));

                throw exception;
            }
        } catch (IOException exception) {
            // this error occurs when the product file is not a zip file.
            throw new InstallationException("", exception);
        }

        ontologyFileService.setInstallFinished(productDir);
    }

}
