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
package edu.pitt.dbmi.ontology.store.ws.service;

import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * Mar 1, 2022 2:59:55 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CrcInstallerService extends AbstractInstallerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstallerService.class);

    private static final String CONCEPT_DIMENSION_TABLE = "concept_dimension";

    private static final String QT_BREAKDOWN_PATH_TABLE = "qt_breakdown_path";
    protected static final String QT_BREAKDOWN_PATH_TABLE_PK = "name";

    @Autowired
    public CrcInstallerService(FileSysService fileSysService) {
        super(fileSysService);
    }

    @Override
    public ActionSummary install(JdbcTemplate jdbcTemplate, OntologyProductAction action) {
        String productFolder = action.getKey().replaceAll(".json", "");

        // import crc data
        for (Path crcData : fileSysService.getCrcData(productFolder)) {
            String fileName = crcData.getFileName().toString();
            String tableName = fileName.replaceAll("_CD.tsv", "");
            try {
                if (!tableExists(jdbcTemplate, tableName)) {
                    insertIntoConceptDimensionTable(jdbcTemplate, crcData);
                }
            } catch (SQLException | IOException exception) {
                String errMsg = String.format("Failed to import ontology from file '%s'.", crcData.toString());
                LOGGER.error(errMsg, exception);

                fileSysService.createInstallFailedIndicatorFile(productFolder);

                return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "CRC Data Installation Failed.");
            }
        }

        // import qt-breakdown-path data
        try {
            insertIntoQtBreakdownPathTable(jdbcTemplate, fileSysService.getQtBreakdownPathFile(productFolder));
        } catch (SQLException | IOException exception) {
            LOGGER.error("QT_BREAKDOWN_PATH.tsv insertion error.", exception);
            fileSysService.createInstallFailedIndicatorFile(productFolder);

            return new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "CRC Installation Failed.");
        }

        fileSysService.createInstallFinishedIndicatorFile(productFolder);

        return new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "CRC Data Installed.");
    }

    private void insertIntoQtBreakdownPathTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        insertUnique(jdbcTemplate, QT_BREAKDOWN_PATH_TABLE, file, QT_BREAKDOWN_PATH_TABLE_PK);
    }

    private void insertIntoConceptDimensionTable(JdbcTemplate jdbcTemplate, Path file) throws SQLException, IOException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                String sql = String.format("SELECT 1 FROM %s.%s WHERE concept_path = ?;", conn.getSchema(), CONCEPT_DIMENSION_TABLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    // skip header
                    reader.readLine();

                    final int conceptPathIndex = 0;
                    List<String> dataRows = new LinkedList<>();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        String[] fields = TAB_DELIM.split(line, 3);
                        try {
                            pstmt.setString(1, fields[conceptPathIndex]);

                            ResultSet resultSet = pstmt.executeQuery();
                            if (!resultSet.next()) {
                                dataRows.add(line);
                            }
                        } catch (SQLException exception) {
                            exception.printStackTrace(System.err);
                        }

                        if (dataRows.size() == DEFAULT_BATCH_SIZE) {
                            insertIntoConceptDimensionTable(jdbcTemplate, dataRows, file);
                        }
                    }

                    insertIntoConceptDimensionTable(jdbcTemplate, dataRows, file);
                }
            }
        }
    }

    private void insertIntoConceptDimensionTable(JdbcTemplate jdbcTemplate, List<String> dataRows, Path file) throws SQLException, IOException {
        if (dataRows.isEmpty()) {
            return;
        }

        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement
                String sql = createInsertStatement(conn.getSchema(), CONCEPT_DIMENSION_TABLE, fileSysService.getHeaders(file));
                PreparedStatement pstmt = conn.prepareStatement(sql);

                // get columnTypes
                int[] columnTypes = getColumnTypes(pstmt.getParameterMetaData());
                for (String line : dataRows) {
                    try {
                        String[] values = TAB_DELIM.split(line);

                        setColumns(pstmt, columnTypes, values);

                        // add null columns not provided
                        if (values.length < columnTypes.length) {
                            for (int i = values.length; i < columnTypes.length; i++) {
                                pstmt.setNull(i + 1, Types.NULL);
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace(System.err);
                    }

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                pstmt.clearBatch();
                dataRows.clear();
            }
        }
    }

}
