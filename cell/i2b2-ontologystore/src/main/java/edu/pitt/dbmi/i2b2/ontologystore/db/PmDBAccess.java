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
package edu.pitt.dbmi.i2b2.ontologystore.db;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ProjectType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 *
 * Oct 14, 2022 1:15:13 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Component
public class PmDBAccess {

    private static final Log LOGGER = LogFactory.getLog(PmDBAccess.class);

    public static final String PM_ENDPOINT_REFERENCE = "ontology.ws.pm.url";
    public static final String ONTSTORE_PRODUCT_LIST_URL = "ontstore.product.list.url";
    public static final String DOWNLOAD_DIR_CELL_PARAM = "ontstore.dir.download";

    public PmDBAccess() {
    }

    public ProjectType getRoleInfo(ConfigureType configureType, MessageHeaderType header) {
        if (configureType != null) {
            for (ProjectType projectType : configureType.getUser().getProject()) {
                LOGGER.debug("Matching PM response's project  [" + projectType.getId() + "] with the request  project [" + header.getProjectId() + "]");
                if (projectType.getId().equals(header.getProjectId())) {
                    return projectType;
                }
            }
        }

        return null;
    }

    public boolean isAdmin(ConfigureType configureType) {
        if (configureType != null) {
            return configureType.getUser().isIsAdmin();
        }

        return false;
    }

}
