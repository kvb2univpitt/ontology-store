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
import edu.pitt.dbmi.i2b2.ontologystore.db.CrcDbSource;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Oct 22, 2022 4:42:45 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class CrcInstallService extends AbstractInstallService {

    private static final Log LOGGER = LogFactory.getLog(CrcInstallService.class);

    private static final String QT_BREAKDOWN_PATH_TABLE = "qt_breakdown_path";
    protected static final String QT_BREAKDOWN_PATH_TABLE_PK = "name";

    @Autowired
    public CrcInstallService(FileSystemService fileSystemService) {
        super(fileSystemService);
    }

    public void createConceptDimension(CrcDbSource crcDbSource, PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        String[] conceptDimensionFiles = packageFile.getConceptDimensions();
        for (String conceptDimensionFile : conceptDimensionFiles) {
            Path zipFilePath = Paths.get(rootFolder, conceptDimensionFile);
            try {
                String tableName = getTableNameFromFileName(zipFilePath);
                if (!conceptDimensionExists(crcDbSource, crcJdbcTemplate, tableName)) {
                    ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());

                    importConceptDimension(crcDbSource, crcJdbcTemplate, tableName, zipEntry, zipFile);
                }
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    public void insertIntoQtBreakdownPathTable(CrcDbSource crcDbSource, PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        String[] breakdownPathFiles = packageFile.getBreakdownPath();
        for (String breakdownPathFile : breakdownPathFiles) {
            Path zipFilePath = Paths.get(rootFolder, breakdownPathFile);
            try {
                ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());
                insertQtBreakdownPath(crcDbSource, crcJdbcTemplate, zipEntry, zipFile);
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    private void insertQtBreakdownPath(CrcDbSource crcDbSource, JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        insertUnique(crcDbSource, jdbcTemplate, QT_BREAKDOWN_PATH_TABLE, zipEntry, zipFile, QT_BREAKDOWN_PATH_TABLE_PK);
    }

    private void importConceptDimension(CrcDbSource crcDbSource, JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        createConceptDimensionTable(jdbcTemplate, tableName);
        insertIntoConceptDimensionTableTable(crcDbSource, jdbcTemplate, tableName, zipEntry, zipFile);
        createConceptDimensionTableIndices(jdbcTemplate, tableName);
    }

    private void createConceptDimensionTableIndices(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        createTableIndexes(jdbcTemplate, tableName, Paths.get("ont", "concept_dimension_indices.sql"));
    }

    private void insertIntoConceptDimensionTableTable(CrcDbSource crcDbSource, JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        batchInsert(crcDbSource, jdbcTemplate, tableName, zipEntry, zipFile, DEFAULT_BATCH_SIZE);
    }

    private void createConceptDimensionTable(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        switch (simplifiedDatabaseVendorName(getDatabaseVendor(jdbcTemplate))) {
            case "postgresql" ->
                createTable(jdbcTemplate, tableName, Paths.get("ont", "postgresql", "concept_dimension_table.sql"));
            case "oracle" ->
                createTable(jdbcTemplate, tableName, Paths.get("ont", "oracle", "concept_dimension_table.sql"));
            case "sqlserver" ->
                createTable(jdbcTemplate, tableName, Paths.get("ont", "sqlserver", "concept_dimension_table.sql"));
        }
    }

    private boolean conceptDimensionExists(CrcDbSource crcDbSource, JdbcTemplate jdbcTemplate, String tableName) throws SQLException {
        return tableExists(crcDbSource, jdbcTemplate, tableName);
    }

}
