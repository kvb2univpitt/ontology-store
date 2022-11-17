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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Nov 16, 2022 6:07:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyDisableService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyDisableService.class);

    private static final String DISABLE_ACTION_TYPE = "Disable";
    private static final String ENABLE_ACTION_TYPE = "Enable";

    private final FileSysService fileSysService;
    private final HiveDBAccess hiveDBAccess;
    private final OntInstallerService ontInstallerService;

    @Autowired
    public OntologyDisableService(FileSysService fileSysService, HiveDBAccess hiveDBAccess, OntInstallerService ontInstallerService) {
        this.fileSysService = fileSysService;
        this.hiveDBAccess = hiveDBAccess;
        this.ontInstallerService = ontInstallerService;
    }

    public synchronized void performDisableEnable(String project, List<ProductActionType> actions, List<ActionSummaryType> summaries) throws InstallationException {
        actions = actions.stream().filter(e -> e.isDisableEnable()).collect(Collectors.toList());
        actions = validate(actions, summaries);

        if (!actions.isEmpty()) {
            String ontJNDIName = hiveDBAccess.getOntDataSourceJNDIName(project);
            if (ontJNDIName == null) {
                throw new InstallationException(String.format("No i2b2 datasource(s) associated with project '%s'.", project));
            }

            DataSource ontDataSource = getDataSource(ontJNDIName);
            if (ontDataSource == null) {
                throw new InstallationException(String.format("No i2b2 JNDI datasource(s) found for project '%s'.", project));
            }

            JdbcTemplate ontJdbcTemplate = new JdbcTemplate(ontDataSource);
            actions.forEach(action -> disableEnable(action, ontJdbcTemplate, summaries));
        }
    }

    private List<ProductActionType> validate(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductActionType> disableEnableActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            if (fileSysService.hasFinshedDownload(productFolder) && fileSysService.hasFinshedInstall(productFolder)) {
                disableEnableActions.add(action);
            } else {
                summaries.add(createActionSummary(action.getTitle(), DISABLE_ACTION_TYPE, false, false, "Ontology not installed."));
            }
        });

        return disableEnableActions;
    }

    private void disableEnable(ProductActionType action, JdbcTemplate ontJdbcTemplate, List<ActionSummaryType> summaries) {
        String productFolder = action.getKey().replaceAll(".json", "");
        boolean enable = fileSysService.hasOntologyDisabled(productFolder);
        Map<String, Path> metadataTableFiles = fileSysService.getMetadata(productFolder).stream()
                .collect(Collectors.toMap(e -> e.getFileName().toString().replaceAll(".tsv", ""), e -> e));
        Map<String, Path> tableAccessTableFiles = fileSysService.getTableAccess(productFolder).stream()
                .collect(Collectors.toMap(e -> e.getFileName().toString().replaceAll("_TA.tsv", ""), e -> e));
        try {
            // remove/add from database
            for (String tableName : metadataTableFiles.keySet()) {
                if (enable) {
                    ontInstallerService.insertIntoTableAccessTable(ontJdbcTemplate, tableAccessTableFiles.get(tableName));
                } else {
                    ontInstallerService.deleteFromTableAccessTable(ontJdbcTemplate, tableName);
                }
            }

            // remove/addd indicator file
            if (enable) {
                fileSysService.removeOntologyDisabledIndicatorFile(productFolder);
                summaries.add(createActionSummary(action.getTitle(), ENABLE_ACTION_TYPE, false, true, "Enabled."));
            } else {
                fileSysService.createOntologyDisabledIndicatorFile(productFolder);
                summaries.add(createActionSummary(action.getTitle(), DISABLE_ACTION_TYPE, false, true, "Disabled."));
            }
        } catch (SQLException | IOException exception) {
            LOGGER.error("", exception);
            if (enable) {
                summaries.add(createActionSummary(action.getTitle(), ENABLE_ACTION_TYPE, false, false, "Enable Ontology Failed."));
            } else {
                summaries.add(createActionSummary(action.getTitle(), DISABLE_ACTION_TYPE, false, false, "Disable Ontology Failed."));
            }
        }
    }

}
