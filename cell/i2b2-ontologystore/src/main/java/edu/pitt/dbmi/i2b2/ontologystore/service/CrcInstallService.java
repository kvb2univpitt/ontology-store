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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
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

    private static final String CONCEPT_DIMENSION_TABLE = "concept_dimension";

    private static final String QT_BREAKDOWN_PATH_TABLE = "qt_breakdown_path";
    protected static final String QT_BREAKDOWN_PATH_TABLE_PK = "name";

    public CrcInstallService(FileSysService fileSysService) {
        super(fileSysService);
    }

    public void insertIntoConceptDimensionTable(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries, ZipFile zipFile, JdbcTemplate crcJdbcTemplate) throws InstallationException {
        String[] conceptDimensionFiles = packageFile.getConceptDimensions();
        for (String conceptDimensionFile : conceptDimensionFiles) {
            Path zipFilePath = Paths.get(rootFolder, conceptDimensionFile);
            try {
                ZipEntry zipEntry = zipEntries.get(zipFilePath.toString());
                insertConceptDimension(crcJdbcTemplate, zipEntry, zipFile);
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

    private List<String> filterConceptDimensionUniquePkey(Connection conn, String schema, Map<String, String> dbData) throws SQLException, IOException {
        List<String> entries = new LinkedList<>();

        String sql = String.format("SELECT 1 FROM %s.%s WHERE concept_path = ?", schema, CONCEPT_DIMENSION_TABLE);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String pkey : dbData.keySet()) {
                pstmt.setString(1, pkey);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (!resultSet.next()) {
                        entries.add(dbData.get(pkey));
                    }
                }
            }
        }

        return entries;
    }

    private void batchInsertConceptDimension(Connection conn, String schema, List<String> headers, List<String> entries) throws SQLException, IOException {
        String sql = createInsertStatement(schema, CONCEPT_DIMENSION_TABLE, headers);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // get columnTypes
            int[] columnTypes = getColumnTypes(pstmt.getParameterMetaData());
            for (String entry : entries) {
                try {
                    // add dummy value ($) at the beganing and end of the line before splitting
                    String[] temp = TAB_DELIM.split(entry);

                    // create a new array of data without the dummy values
                    String[] values = new String[temp.length - 2];
                    System.arraycopy(temp, 1, values, 0, values.length);

                    // concept path not existed, add to batch for insert
                    setColumns(pstmt, columnTypes, values);

                    // add null columns not provided
                    if (values.length < columnTypes.length) {
                        for (int i = values.length; i < columnTypes.length; i++) {
                            pstmt.setNull(i + 1, Types.NULL);
                        }
                    }

                    pstmt.addBatch();
                } catch (Exception exception) {
                    exception.printStackTrace(System.err);
                }
            }

            pstmt.executeBatch();
        }
    }

    private void insertConceptDimension(JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
            List<String> headers = getHeaders(reader.readLine());
            String schema = conn.getSchema();

            Map<String, String> dbData = new HashMap<>();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                // skip lines that are commented out
                String cleanedLine = line.trim();
                if (cleanedLine.isEmpty() || cleanedLine.startsWith("--")) {
                    continue;
                }

                String entry = String.format("$\t%s\t$", line);
                String[] values = TAB_DELIM.split(entry, 3);
                String pkey = values[1].trim();
                dbData.put(pkey, entry);

                if (dbData.size() == DEFAULT_BATCH_SIZE) {
                    List<String> entries = filterConceptDimensionUniquePkey(conn, schema, dbData);
                    batchInsertConceptDimension(conn, schema, headers, entries);
                    dbData.clear();
                }
            }

            if (!dbData.isEmpty()) {
                List<String> entries = filterConceptDimensionUniquePkey(conn, schema, dbData);
                batchInsertConceptDimension(conn, schema, headers, entries);
            }
        }
    }

    private void insertQtBreakdownPath(JdbcTemplate jdbcTemplate, ZipEntry zipEntry, ZipFile zipFile) throws SQLException, IOException {
        insertUnique(jdbcTemplate, QT_BREAKDOWN_PATH_TABLE, zipEntry, zipFile, QT_BREAKDOWN_PATH_TABLE_PK);
    }

}
