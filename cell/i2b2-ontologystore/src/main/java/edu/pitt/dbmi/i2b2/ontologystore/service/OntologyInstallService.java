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

import edu.pitt.dbmi.i2b2.ontologystore.InstallationException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Oct 22, 2022 4:24:39 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyInstallService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final FileSysService fileSysService;
    private final HiveDBAccess hiveDBAccess;
    private final OntInstallerService ontInstallerService;
    private final CrcInstallerService crcInstallerService;

    @Autowired
    public OntologyInstallService(
            FileSysService fileSysService,
            HiveDBAccess hiveDBAccess,
            OntInstallerService ontInstallerService,
            CrcInstallerService crcInstallerService) {
        this.fileSysService = fileSysService;
        this.hiveDBAccess = hiveDBAccess;
        this.ontInstallerService = ontInstallerService;
        this.crcInstallerService = crcInstallerService;
    }

    public synchronized void performInstallation(String project, List<ProductActionType> actions, List<ActionSummaryType> summaries) throws InstallationException {
        actions = actions.stream().filter(e -> !e.isDisableEnable() && e.isInstall()).collect(Collectors.toList());
        actions = validateProgress(actions, summaries);
        actions = validateFilesExistence(actions, summaries);

        if (!actions.isEmpty()) {
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

            // prepare for installation
            actions.forEach(action -> {
                String productFolder = action.getKey().replaceAll(".json", "");
                fileSysService.createInstallStartedIndicatorFile(productFolder);
            });

            // install
            JdbcTemplate ontJdbcTemplate = new JdbcTemplate(ontDataSource);
            JdbcTemplate crcJdbcTemplate = new JdbcTemplate(crcDataSource);
            actions.forEach(action -> install(action, ontJdbcTemplate, crcJdbcTemplate, summaries));
        }
    }

    private void install(ProductActionType action, JdbcTemplate ontJdbcTemplate, JdbcTemplate crcJdbcTemplate, List<ActionSummaryType> summaries) {
        String productFolder = action.getKey().replaceAll(".json", "");

        Map<String, Path> metadataTableFiles = fileSysService.getMetadata(productFolder).stream()
                .collect(Collectors.toMap(e -> e.getFileName().toString().replaceAll(".tsv", ""), e -> e));
        Map<String, Path> tableAccessTableFiles = fileSysService.getTableAccess(productFolder).stream()
                .collect(Collectors.toMap(e -> e.getFileName().toString().replaceAll("_TA.tsv", ""), e -> e));
        Map<String, Path> crcDataTableFiles = fileSysService.getCrcData(productFolder).stream()
                .collect(Collectors.toMap(e -> e.getFileName().toString().replaceAll("_CD.tsv", ""), e -> e));

        Set<String> createdMetadataTables = new HashSet<>();

        // install metadata
        try {
            for (String tableName : metadataTableFiles.keySet()) {
                if (!ontInstallerService.metadataExists(ontJdbcTemplate, tableName)) {
                    ontInstallerService.importMetadata(ontJdbcTemplate, tableName, metadataTableFiles.get(tableName));
                    ontInstallerService.insertIntoTableAccessTable(ontJdbcTemplate, tableAccessTableFiles.get(tableName));
                    createdMetadataTables.add(tableName);
                }
            }
            ontInstallerService.insertIntoSchemesTable(ontJdbcTemplate, fileSysService.getSchemesFile(productFolder));

            summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Metadata Installed."));
        } catch (SQLException | IOException exception) {
            LOGGER.error("", exception);
            summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Metadata Installation Failed."));
            fileSysService.createInstallFailedIndicatorFile(productFolder);
        }

        // install CRC data
        try {
            for (String tableName : createdMetadataTables) {
                crcInstallerService.importCrcData(crcJdbcTemplate, crcDataTableFiles.get(tableName));
            }
            crcInstallerService.insertIntoQtBreakdownPathTable(crcJdbcTemplate, fileSysService.getQtBreakdownPathFile(productFolder));

            summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, true, "CRC Data Installed."));
        } catch (SQLException | IOException exception) {
            LOGGER.error("", exception);
            summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "CRC Data Installation Failed."));
            fileSysService.createInstallFailedIndicatorFile(productFolder);
        }

        fileSysService.createInstallFinishedIndicatorFile(productFolder);
    }

    private List<ProductActionType> validateProgress(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductActionType> installActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            if (fileSysService.hasFinshedDownload(productFolder)) {
                if (fileSysService.hasFinshedInstall(productFolder)) {
                    summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Already Installed."));
                } else if (fileSysService.hasFailedInstall(productFolder)) {
                    summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation previously failed."));
                } else if (fileSysService.hasStartedInstall(productFolder)) {
                    summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, true, false, "Installation already started."));
                } else {
                    installActions.add(action);
                }
            } else if (fileSysService.hasFailedDownload(productFolder)) {
                summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download previously failed."));
            } else if (fileSysService.hasStartedDownload(productFolder)) {
                summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download not finished."));
            } else {
                summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Has not been downloaded."));
            }
        });

        return installActions;
    }

    private List<ProductActionType> validateFilesExistence(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductActionType> installActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");

            Set<String> metadata = new HashSet<>();
            Set<String> crcData = new HashSet<>();
            Set<String> tableAccess = new HashSet<>();
            fileSysService.getMetadata(productFolder).stream()
                    .map(e -> e.getFileName().toString().replaceAll(".tsv", ""))
                    .forEach(metadata::add);
            fileSysService.getCrcData(productFolder).stream()
                    .map(e -> e.getFileName().toString().replaceAll("_CD.tsv", ""))
                    .forEach(crcData::add);
            fileSysService.getTableAccess(productFolder).stream()
                    .map(e -> e.getFileName().toString().replaceAll("_TA.tsv", ""))
                    .forEach(tableAccess::add);

            boolean isValid = true;
            for (String tableName : metadata) {
                isValid = crcData.contains(tableName);
                if (!isValid) {
                    summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, String.format("Missing %s_CD.tsv file", tableName)));
                    break;
                }

                isValid = tableAccess.contains(tableName);
                if (!isValid) {
                    summaries.add(createActionSummary(action.getTitle(), ACTION_TYPE, false, false, String.format("Missing %s_TA.tsv file", tableName)));
                    break;
                }
            }

            if (isValid) {
                installActions.add(action);
            } else {
                fileSysService.createInstallFailedIndicatorFile(productFolder);
            }
        });

        return installActions;
    }

}
