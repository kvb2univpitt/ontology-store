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
package edu.pitt.dbmi.i2b2.ontologystore.pm;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.StatusType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.util.OntologyStoreJAXBUtil;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 12, 2022 9:10:30 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class PMResponseMessage {

    private static final Log LOGGER = LogFactory.getLog(PMResponseMessage.class);

    private ResponseMessageType pmRespMessageType = null;

    public PMResponseMessage() {
    }

    public StatusType processResult(String response) {
        try {
            JAXBElement jaxbElement = OntologyStoreJAXBUtil.getJaxbUtil().unMashallFromString(response);
            pmRespMessageType = (ResponseMessageType) jaxbElement.getValue();
        } catch (JAXBUtilException e) {
            LOGGER.error(e.getMessage());
            return null;
        }

        // Get response message status 
        ResponseHeaderType responseHeader = pmRespMessageType.getResponseHeader();

        StatusType status = responseHeader.getResultStatus().getStatus();
        String procStatus = status.getType();
        String procMessage = status.getValue();

        if (procStatus.equals("ERROR")) {
            LOGGER.error("Error reported by Ont web Service " + procMessage);
        } else if (procStatus.equals("WARNING")) {
            LOGGER.debug("Warning reported by Ont web Service" + procMessage);
        }

        return status;
    }

    public ConfigureType readUserInfo() throws Exception {
        BodyType bodyType = pmRespMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();

        return (ConfigureType) helper.getObjectByClass(bodyType.getAny(), ConfigureType.class);
    }

}
