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
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Oct 22, 2022 4:42:45 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class CrcInstallService extends AbstractInstallService {

    private static final Log LOGGER = LogFactory.getLog(CrcInstallService.class);

    private static final String QT_BREAKDOWN_PATH_TABLE = "qt_breakdown_path";
    protected static final String QT_BREAKDOWN_PATH_TABLE_PK = "name";

    public CrcInstallService(FileSysService fileSysService) {
        super(fileSysService);
    }

    public void createConceptDimension(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        String[] conceptDimensionFiles = packageFile.getConceptDimensions();
        for (String conceptDimensionFile : conceptDimensionFiles) {
            Path zipFilePath = Paths.get(rootFolder, conceptDimensionFile);
            try {
                String tableName = zipFilePath.getFileName().toString().replace(".tsv", "").replace(".TSV", "");
                if (!conceptDimensionExists(crcJdbcTemplate, tableName)) {
                    ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());

                    importConceptDimension(crcJdbcTemplate, tableName, zipEntry, zipFile);
                }
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    public void insertIntoQtBreakdownPathTable(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        String[] breakdownPathFiles = packageFile.getBreakdownPath();
        for (String breakdownPathFile : breakdownPathFiles) {
            Path zipFilePath = Paths.get(rootFolder, breakdownPathFile);
            try {
                ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());
                insertQtBreakdownPath(crcJdbcTemplate, zipEntry, zipFile);
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    private void insertQtBreakdownPath(JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        insertUnique(jdbcTemplate, QT_BREAKDOWN_PATH_TABLE, zipEntry, zipFile, QT_BREAKDOWN_PATH_TABLE_PK);
    }

    private void importConceptDimension(JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        createConceptDimensionTable(jdbcTemplate, tableName);
        insertIntoConceptDimensionTableTable(jdbcTemplate, tableName, zipEntry, zipFile);
        createConceptDimensionTableIndices(jdbcTemplate, tableName);
    }

    private void createConceptDimensionTableIndices(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        createTableIndexes(jdbcTemplate, tableName, Paths.get("ont", "concept_dimension_indices.sql"));
    }

    private void insertIntoConceptDimensionTableTable(JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        batchInsert(jdbcTemplate, tableName, zipEntry, zipFile, DEFAULT_BATCH_SIZE);
    }

    private void createConceptDimensionTable(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        switch (getDatabaseVendor(jdbcTemplate)) {
            case "PostgreSQL":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "postgresql", "concept_dimension_table.sql"));
                break;
            case "Oracle":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "oracle", "concept_dimension_table.sql"));
                break;
            case "Microsoft SQL Server":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "sqlserver", "concept_dimension_table.sql"));
                break;
        }
    }

    private boolean conceptDimensionExists(JdbcTemplate jdbcTemplate, String tableName) throws SQLException {
        return tableExists(jdbcTemplate, tableName);
    }

}
