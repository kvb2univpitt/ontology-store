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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.StatusType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.CellDataType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.GetUserConfigurationType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ParamType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ProjectType;
import edu.pitt.dbmi.i2b2.ontologystore.pm.GetUserConfigurationRequestMessage;
import edu.pitt.dbmi.i2b2.ontologystore.pm.PMResponseMessage;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.sql.DataSource;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * Oct 14, 2022 1:15:13 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class PmDBAccess {

    private static final Log LOGGER = LogFactory.getLog(PmDBAccess.class);
    private static final Logger API_LOGGER = ESAPI.getLogger(PmDBAccess.class);

    public static final String PM_ENDPOINT_REFERENCE = "ontology.ws.pm.url";
    public static final String ONTSTORE_PRODUCT_LIST_URL = "ontstore.product.list.url";
    public static final String DOWNLOAD_DIR_CELL_PARAM = "ontstore.dir.download";

    private final JdbcTemplate hiveJdbcTemplate;

    @Autowired
    public PmDBAccess(DataSource hiveDataSource) {
        this.hiveJdbcTemplate = new JdbcTemplate(hiveDataSource);
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

    public String getDownloadDirectory(ConfigureType configureType) throws I2B2Exception {
        if (configureType == null) {
            throw new I2B2Exception("Cell parameters has not been set.");
        }

        Map<String, String> cellParams = getCellParameters(configureType);
        if (!cellParams.containsKey(DOWNLOAD_DIR_CELL_PARAM)) {
            throw new I2B2Exception("Parameter '" + DOWNLOAD_DIR_CELL_PARAM + "' has not been set.");
        }

        return cellParams.get(DOWNLOAD_DIR_CELL_PARAM);
    }

    private Map<String, String> getCellParameters(ConfigureType configureType) {
        Map<String, String> cellParams = new HashMap<>();

        if (configureType != null) {
            for (CellDataType data : configureType.getCellDatas().getCellData()) {
                for (ParamType paramType : data.getParam()) {
                    cellParams.put(paramType.getName(), paramType.getValue());
                }
            }
        }

        return cellParams;
    }

    public ConfigureType getConfigureType(MessageHeaderType header) {
        try {
            PMResponseMessage msg = new PMResponseMessage();
            String response = getUserConfigurationResponsetMessage(new GetUserConfigurationType(), header);
            API_LOGGER.debug(null, response);
            StatusType procStatus = msg.processResult(response);
            if (procStatus.getType().equals("ERROR")) {
                return null;
            }

            return msg.readUserInfo();
        } catch (AxisFault e) {
            LOGGER.error("Cant connect to PM service");
        } catch (I2B2Exception e) {
            LOGGER.error("Problem processing PM service address");
        } catch (Exception e) {
            LOGGER.error("General PM processing problem:  " + e.getMessage());
        }

        return null;
    }

    private String getUserConfigurationResponsetMessage(GetUserConfigurationType userConfig, MessageHeaderType header) throws Exception {
        GetUserConfigurationRequestMessage reqMsg = new GetUserConfigurationRequestMessage();
        String getRolesRequestString = reqMsg.doBuildXML(userConfig, header);
        try {
            String response = ServiceClient.sendREST(getPmEndpointReference(), getRolesRequestString);
            LOGGER.debug("PM response = " + response);

            return response;
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            throw exception;
        }
    }

    private List<ParamType> getParamTypes(String cellId) throws I2B2Exception {
        try {
            String schema = getSchema(hiveJdbcTemplate.getDataSource());
            String sql = "SELECT * FROM " + schema + ".hive_cell_params WHERE status_cd <> 'D' AND cell_id = '" + cellId + "'";

            return hiveJdbcTemplate.query(sql, new HiveCellParam());
        } catch (DataAccessException | SQLException exception) {
            throw new I2B2DAOException("Database error");
        }
    }

    private String getPropertyValue(String propertyName, String cellId) throws I2B2Exception {
        String propertyValue = null;
        for (ParamType paramType : getParamTypes(cellId)) {
            String name = paramType.getName();
            if (name != null && name.equalsIgnoreCase(propertyName)) {
                String dataType = paramType.getDatatype();
                if (dataType.equalsIgnoreCase("U")) {
                    try {
                        propertyValue = ServiceClient.getContextRoot() + paramType.getValue();
                    } catch (AttributeNotFoundException | AxisFault
                            | InstanceNotFoundException | MBeanException
                            | MalformedObjectNameException | ReflectionException exception) {
                        exception.printStackTrace(System.err);
                    }
                } else {
                    propertyValue = paramType.getValue();
                }

                break;
            }
        }

        if ((propertyValue == null) || (propertyValue.trim().isEmpty())) {
            throw new I2B2Exception("Application property file(" + propertyName + " entry");
        }

        return propertyValue;
    }

    /**
     * Return PM cell endpoint reference URL
     *
     * @return
     * @throws I2B2Exception
     */
    private String getPmEndpointReference() throws I2B2Exception {
        return getPropertyValue(PM_ENDPOINT_REFERENCE, "ONT").trim();
    }

    public String getOntStoreProductListUrl() throws I2B2Exception {
        return getPropertyValue(ONTSTORE_PRODUCT_LIST_URL, "ONTSTORE").trim();
    }

    private String getSchema(DataSource dataSource) throws SQLException {
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                return conn.getSchema();
            }
        }

        return null;
    }

}
