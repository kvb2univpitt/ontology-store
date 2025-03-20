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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Oct 22, 2022 4:33:21 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class MetadataInstallService extends AbstractInstallService {

    private static final Log LOGGER = LogFactory.getLog(MetadataInstallService.class);

    protected static final String SCHEMES_TABLE_NAME = "schemes";
    protected static final String SCHEMES_TABLE_PK = "c_key";

    protected static final String TABLE_ACCESS_TABLE_NAME = "table_access";
    protected static final String TABLE_ACCESS_TABLE_NAME_COLUMN = "c_table_name";
    protected static final String TABLE_ACCESS_TABLE_PK = "c_table_cd";

    public MetadataInstallService(FileSysService fileSysService) {
        super(fileSysService);
    }

    public void deleteFromTableAccessTable(PackageFile packageFile, JdbcTemplate ontJdbcTemplate) throws IOException, SQLException {
        List<String> tableNames = getMetadataTableNames(packageFile);
        deleteFromTableAccess(ontJdbcTemplate, TABLE_ACCESS_TABLE_NAME, TABLE_ACCESS_TABLE_NAME_COLUMN, tableNames);
    }

    public void insertIntoTableAccessTable(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate ontJdbcTemplate) throws InstallationException {
        String[] tableAccessFiles = packageFile.getTableAccess();
        for (String tableAccessFile : tableAccessFiles) {
            Path zipFilePath = Paths.get(rootFolder, tableAccessFile);
            try {
                ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());
                insertTableAccess(ontJdbcTemplate, zipEntry, zipFile);
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    public void insertIntoSchemesTable(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate ontJdbcTemplate) throws InstallationException {
        String[] schemeFiles = packageFile.getSchemes();
        for (String schemeFile : schemeFiles) {
            Path zipFilePath = Paths.get(rootFolder, schemeFile);
            try {
                ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());
                insertSchemes(ontJdbcTemplate, zipEntry, zipFile);
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    public void createMetadata(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate ontJdbcTemplate) throws InstallationException {
        String[] ontologyFiles = packageFile.getDomainOntologies();
        for (String ontologyFile : ontologyFiles) {
            Path zipFilePath = Paths.get(rootFolder, ontologyFile);
            try {
                String tableName = zipFilePath.getFileName().toString().replace(".tsv", "").replace(".TSV", "");
                if (!metadataExists(ontJdbcTemplate, tableName)) {
                    ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());

                    importMetadata(ontJdbcTemplate, tableName, zipEntry, zipFile);
                }
            } catch (SQLException | IOException exception) {
                LOGGER.error("", exception);
                throw new InstallationException(exception.getMessage());
            }
        }
    }

    private List<String> getMetadataTableNames(PackageFile packageFile) {
        List<String> tableNames = new LinkedList<>();

        String[] ontologyFiles = packageFile.getDomainOntologies();
        for (String ontologyFile : ontologyFiles) {
            Path zipFilePath = Paths.get(ontologyFile);
            String tableName = zipFilePath.getFileName().toString().replace(".tsv", "").replace(".TSV", "");

            tableNames.add(tableName);
        }

        return tableNames;
    }

    private void insertTableAccess(JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        insertUnique(jdbcTemplate, TABLE_ACCESS_TABLE_NAME, zipEntry, zipFile, TABLE_ACCESS_TABLE_PK);
    }

    private void insertSchemes(JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        insertUnique(jdbcTemplate, SCHEMES_TABLE_NAME, zipEntry, zipFile, SCHEMES_TABLE_PK);
    }

    private void importMetadata(JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        createOntologyTable(jdbcTemplate, tableName);
        insertIntoOntologyTable(jdbcTemplate, tableName, zipEntry, zipFile);
        createOntologyTableIndices(jdbcTemplate, tableName);
    }

    private void createOntologyTableIndices(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        createTableIndexes(jdbcTemplate, tableName, Paths.get("ont", "ontology_table_indices.sql"));
    }

    private void insertIntoOntologyTable(JdbcTemplate jdbcTemplate, String tableName, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        batchInsert(jdbcTemplate, tableName, zipEntry, zipFile, DEFAULT_BATCH_SIZE);
    }

    private void createOntologyTable(JdbcTemplate jdbcTemplate, String tableName) throws SQLException, IOException {
        switch (getDatabaseVendor(jdbcTemplate)) {
            case "PostgreSQL":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "postgresql", "ontology_table.sql"));
                break;
            case "Oracle":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "oracle", "ontology_table.sql"));
                break;
            case "Microsoft SQL Server":
                createTable(jdbcTemplate, tableName, Paths.get("ont", "sqlserver", "ontology_table.sql"));
                break;
        }
    }

    private boolean metadataExists(JdbcTemplate jdbcTemplate, String tableName) throws SQLException {
        return tableExists(jdbcTemplate, tableName);
    }

}
