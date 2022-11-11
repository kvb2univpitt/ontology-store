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

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.GetUserConfigurationType;
import edu.pitt.dbmi.i2b2.ontologystore.util.OntologyStoreJAXBUtil;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 12, 2022 5:42:23 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetUserConfigurationRequestMessage extends ProjectManagementRequestData {

    private static final Log LOGGER = LogFactory.getLog(GetUserConfigurationRequestMessage.class);

    /**
     * Function to convert Ont Request message type to an XML string.
     *
     * @param reqMessageType String containing Ont request message to be
     * converted to string
     * @return A String data type containing the Ont RequestMessage in XML
     * format
     * @throws JAXBUtilException
     */
    public String getXMLString(RequestMessageType reqMessageType) throws JAXBUtilException {
        try (StringWriter writer = new StringWriter()) {
            JAXBElement<RequestMessageType> element = (new edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ObjectFactory())
                    .createRequest(reqMessageType);
            OntologyStoreJAXBUtil.getJaxbUtil().marshaller(element, writer);

            return writer.toString();
        } catch (JAXBUtilException exception) {
            LOGGER.error("Error marshalling Ont request message");
            throw exception;
        } catch (IOException exception) {
            LOGGER.error("Error marshalling Ont request message");
            throw new JAXBUtilException("", exception);
        }
    }

    /**
     * Function to build getUserConfiguration body type.
     *
     * @param userConfigurationType
     * @return BodyType object
     */
    public BodyType getBodyType(GetUserConfigurationType userConfigurationType) {
        JAXBElement<GetUserConfigurationType> element = (new edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ObjectFactory())
                .createGetUserConfiguration(userConfigurationType);

        BodyType bodyType = new BodyType();
        bodyType.getAny().add(element);

        return bodyType;
    }

    /**
     * Function to build PM Request message type and return it as an XML string.
     *
     * @param userConfig
     * @param header
     * @return A String data type containing the PM RequestMessage in XML format
     */
    public String doBuildXML(GetUserConfigurationType userConfig, MessageHeaderType header) {
        try {
            MessageHeaderType messageHeader = getMessageHeader();
            messageHeader.setSecurity(header.getSecurity());
            messageHeader.setProjectId(header.getProjectId());

            RequestHeaderType reqHeader = getRequestHeader();
            BodyType bodyType = getBodyType(userConfig);
            RequestMessageType reqMessageType = getRequestMessageType(messageHeader, reqHeader, bodyType);

            return getXMLString(reqMessageType);
        } catch (JAXBUtilException exception) {
            LOGGER.error(exception.getMessage());
        }

        return null;
    }

}
