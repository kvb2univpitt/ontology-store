/*
 * Copyright (C) 2021 University of Pittsburgh.
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

import edu.pitt.dbmi.ontology.store.ws.InstallationException;
import edu.pitt.dbmi.ontology.store.ws.db.HiveDBAccess;
import edu.pitt.dbmi.ontology.store.ws.model.ActionSummary;
import edu.pitt.dbmi.ontology.store.ws.model.OntologyProductAction;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 9, 2021 10:47:02 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyInstallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyInstallService.class);

    private static final String ACTION_TYPE = "Install";

    private final FileSysService fileSysService;
    private final HiveDBAccess hiveDBAccess;
    private final OntInstallerService ontInstallerService;

    @Autowired
    public OntologyInstallService(FileSysService fileSysService, HiveDBAccess hiveDBAccess, OntInstallerService ontInstallerService) {
        this.fileSysService = fileSysService;
        this.hiveDBAccess = hiveDBAccess;
        this.ontInstallerService = ontInstallerService;
    }

    public synchronized void performInstallation(String project, List<OntologyProductAction> actions, List<ActionSummary> summaries) throws InstallationException {
        actions = actions.stream().filter(e -> e.isInstall()).collect(Collectors.toList());
        actions = validate(actions, summaries);

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

            ontInstallerService.install(new JdbcTemplate(ontDataSource), actions, summaries);
        }
    }

    public DataSource getDataSource(String datasourceJNDIName) {
        try {
            return (new JndiDataSourceLookup()).getDataSource(datasourceJNDIName);
        } catch (Exception exception) {
            String errMsg = String.format("Unable to get datasource for JNDI name '%s'.", datasourceJNDIName);
            LOGGER.error(errMsg, exception);
            return null;
        }
    }

    private List<OntologyProductAction> validate(List<OntologyProductAction> actions, List<ActionSummary> summaries) {
        List<OntologyProductAction> installActions = new LinkedList<>();

        actions.forEach(action -> {
            String productFolder = action.getKey().replaceAll(".json", "");
            if (fileSysService.hasFinshedDownload(productFolder)) {
                if (fileSysService.hasFinshedInstall(productFolder)) {
                    summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, true, "Already Installed."));
                } else if (fileSysService.hasFailedInstall(productFolder)) {
                    summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Installation previously failed."));
                } else if (fileSysService.hasStartedInstall(productFolder)) {
                    summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, true, false, "Installation already started."));
                } else {
                    installActions.add(action);
                }
            } else if (fileSysService.hasFailedDownload(productFolder)) {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download previously failed."));
            } else if (fileSysService.hasStartedDownload(productFolder)) {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Download not finished."));
            } else {
                summaries.add(new ActionSummary(action.getTitle(), ACTION_TYPE, false, false, "Has not been downloaded."));
            }
        });

        return installActions;
    }

}
