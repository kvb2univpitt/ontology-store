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
package edu.pitt.dbmi.i2b2.ontologystore.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ApplicationType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.FacilityType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageControlIdType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ProcessingIdType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResultStatusType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.StatusType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummariesType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionsType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductsType;
import edu.pitt.dbmi.i2b2.ontologystore.util.OntologyStoreJAXBUtil;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 6, 2022 7:47:14 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class MessageFactory {

    private static final Log LOGGER = LogFactory.getLog(MessageFactory.class);

    public static OMElement createResponseOMElementFromString(String xmlString) throws I2B2Exception {
        try {
            return AXIOMUtil.stringToOM(xmlString);
        } catch (XMLStreamException exception) {
            return null;
        }
    }

    public static ResponseMessageType doBuildErrorResponse(MessageHeaderType messageHeaderType, String errorMessage) {
        MessageHeaderType messageHeader = createResponseMessageHeader(messageHeaderType);
        ResponseHeaderType respHeader = createResponseHeader("ERROR", errorMessage);

        return createResponseMessageType(messageHeader, respHeader, null);
    }

    /**
     * Function to create response message type
     *
     * @param messageHeader
     * @param respHeader
     * @param bodyType
     * @return ResponseMessageType
     */
    public static ResponseMessageType createResponseMessageType(MessageHeaderType messageHeader, ResponseHeaderType respHeader, BodyType bodyType) {
        ResponseMessageType respMsgType = new ResponseMessageType();
        respMsgType.setMessageHeader(messageHeader);
        respMsgType.setMessageBody(bodyType);
        respMsgType.setResponseHeader(respHeader);

        return respMsgType;
    }

    /**
     * Function to convert ResponseMessageType to string
     *
     * @param respMessageType
     * @return String
     * @throws I2B2Exception
     */
    public static String convertToXMLString(ResponseMessageType respMessageType) throws I2B2Exception {
        try (StringWriter writer = new StringWriter()) {
            edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ObjectFactory objectFactory = new edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ObjectFactory();
            OntologyStoreJAXBUtil.getJaxbUtil().marshaller(objectFactory.createResponse(respMessageType), writer);

            return writer.toString();
        } catch (JAXBUtilException | IOException exception) {
            throw new I2B2Exception("Error converting response message type to string " + exception.getMessage(), exception);
        }
    }

    private static ResponseHeaderType createResponseHeader(String type, String value) {
        StatusType status = new StatusType();
        status.setType(type);
        status.setValue(value);

        ResultStatusType resStat = new ResultStatusType();
        resStat.setStatus(status);

        ResponseHeaderType respHeader = new ResponseHeaderType();
        respHeader.setResultStatus(resStat);

        return respHeader;
    }

    public static MessageHeaderType createResponseMessageHeader(MessageHeaderType reqMsgHeader) {
        MessageHeaderType messageHeader = new MessageHeaderType();
        messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
        messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));

        ApplicationType appType = new ApplicationType();
        appType.setApplicationName("OntologyStore Cell");
        appType.setApplicationVersion("1.700");
        messageHeader.setSendingApplication(appType);

        FacilityType facility = new FacilityType();
        facility.setFacilityName("i2b2 Hive");
        messageHeader.setSendingFacility(facility);

        if (reqMsgHeader != null) {
            ApplicationType recvApp = new ApplicationType();
            recvApp.setApplicationName(reqMsgHeader.getSendingApplication()
                    .getApplicationName());
            recvApp.setApplicationVersion(reqMsgHeader.getSendingApplication()
                    .getApplicationVersion());
            messageHeader.setReceivingApplication(recvApp);

            FacilityType recvFac = new FacilityType();
            recvFac.setFacilityName(reqMsgHeader.getSendingFacility()
                    .getFacilityName());
            messageHeader.setReceivingFacility(recvFac);
            messageHeader.setSecurity(reqMsgHeader.getSecurity());
        }

        Date currentDate = new Date();
        DTOFactory factory = new DTOFactory();
        messageHeader.setDatetimeOfMessage(factory
                .getXMLGregorianCalendar(currentDate.getTime()));

        MessageControlIdType mcIdType = new MessageControlIdType();
        mcIdType.setInstanceNum(1);

        if ((reqMsgHeader != null) && (reqMsgHeader.getMessageControlId() != null)) {
            mcIdType.setMessageNum(reqMsgHeader.getMessageControlId()
                    .getMessageNum());
            mcIdType.setSessionId(reqMsgHeader.getMessageControlId()
                    .getSessionId());
        }

        messageHeader.setMessageControlId(mcIdType);

        ProcessingIdType proc = new ProcessingIdType();
        proc.setProcessingId("P");
        proc.setProcessingMode("I");
        messageHeader.setProcessingId(proc);

        messageHeader.setAcceptAcknowledgementType("AL");
        messageHeader.setApplicationAcknowledgementType("AL");
        messageHeader.setCountryCode("US");
        if (reqMsgHeader != null) {
            messageHeader.setProjectId(reqMsgHeader.getProjectId());
        }
        return messageHeader;
    }

    public static ResponseMessageType buildGetProductsResponse(MessageHeaderType messageHeaderType, ProductsType value) {
        ResponseHeaderType respHeader = createResponseHeader("DONE", "OntologyStore processing completed");

        BodyType bodyType = new BodyType();
        if (value != null) {
            edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory of = new edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory();
            bodyType.getAny().add(of.createProducts(value));
        }

        return createResponseMessageType(messageHeaderType, respHeader, bodyType);
    }

    public static ResponseMessageType buildGetActionSummariesResponse(MessageHeaderType messageHeaderType, ActionSummariesType value) {
        ResponseHeaderType respHeader = createResponseHeader("DONE", "OntologyStore processing completed");

        BodyType bodyType = new BodyType();
        if (value != null) {
            edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory of = new edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory();
            bodyType.getAny().add(of.createActionSummaries(value));
        }

        return createResponseMessageType(messageHeaderType, respHeader, bodyType);
    }

    public static ResponseMessageType buildGetProductActionsType(MessageHeaderType messageHeaderType, ProductActionsType value) {
        ResponseHeaderType respHeader = createResponseHeader("DONE", "OntologyStore processing completed");

        BodyType bodyType = new BodyType();
        if (value != null) {
            edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory of = new edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ObjectFactory();
            bodyType.getAny().add(of.createProductActions(value));
        }

        return createResponseMessageType(messageHeaderType, respHeader, bodyType);
    }

}
