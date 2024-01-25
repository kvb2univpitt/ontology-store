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

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 *
 * Oct 22, 2022 6:47:51 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(AbstractOntologyService.class);

    protected final FileSysService fileSysService;
    protected final OntologyFileService ontologyFileService;

    public AbstractOntologyService(FileSysService fileSysService, OntologyFileService ontologyFileService) {
        this.fileSysService = fileSysService;
        this.ontologyFileService = ontologyFileService;
    }

    protected ActionSummaryType createActionSummary(String title, String actionType, boolean inProgress, boolean success, String detail) {
        ActionSummaryType summary = new ActionSummaryType();
        summary.setTitle(title);
        summary.setActionType(actionType);
        summary.setInProgress(inProgress);
        summary.setSuccess(success);
        summary.setDetail(detail);

        return summary;
    }

    protected DataSource getDataSource(String datasourceJNDIName) {
        try {
            return (new JndiDataSourceLookup()).getDataSource(datasourceJNDIName);
        } catch (Exception exception) {
            String errMsg = String.format("Unable to get datasource for JNDI name '%s'.", datasourceJNDIName);
            LOGGER.error(errMsg, exception);
            return null;
        }
    }

}
