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

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ApplicationType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.FacilityType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageControlIdType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ProcessingIdType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestMessageType;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 12, 2022 5:45:03 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 * @author Mike Mendis
 * @author Raj Kuttan
 */
public abstract class ProjectManagementRequestData {

    private static final Log LOGGER = LogFactory.getLog(ProjectManagementRequestData.class);

    public ProjectManagementRequestData() {
    }

    /**
     * Function to build i2b2 Request message header
     *
     * @return RequestHeader object
     */
    public RequestHeaderType getRequestHeader() {
        RequestHeaderType reqHeader = new RequestHeaderType();
        reqHeader.setResultWaittimeMs(120000);
        return reqHeader;
    }

    /**
     * Function to build i2b2 message header
     *
     * @return MessageHeader object
     */
    public MessageHeaderType getMessageHeader() {
        MessageHeaderType messageHeader = new MessageHeaderType();

        messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
        messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));

        ApplicationType appType = new ApplicationType();
        appType.setApplicationName("Ontology Cell");
        appType.setApplicationVersion("1.7");
        messageHeader.setSendingApplication(appType);

        FacilityType facility = new FacilityType();
        facility.setFacilityName("i2b2 Hive");
        messageHeader.setSendingFacility(facility);

        ApplicationType appType2 = new ApplicationType();
        appType2.setApplicationVersion("1.7");
        appType2.setApplicationName("Project Management Cell");
        messageHeader.setReceivingApplication(appType2);

        FacilityType facility2 = new FacilityType();
        facility2.setFacilityName("i2b2 Hive");
        messageHeader.setReceivingFacility(facility2);

        Date currentDate = new Date();
        DTOFactory factory = new DTOFactory();
        messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));

        MessageControlIdType mcIdType = new MessageControlIdType();
        mcIdType.setInstanceNum(0);
        mcIdType.setMessageNum(generateMessageId());
        messageHeader.setMessageControlId(mcIdType);

        ProcessingIdType proc = new ProcessingIdType();
        proc.setProcessingId("P");
        proc.setProcessingMode("I");
        messageHeader.setProcessingId(proc);

        messageHeader.setAcceptAcknowledgementType("AL");
        messageHeader.setApplicationAcknowledgementType("AL");
        messageHeader.setCountryCode("US");

        return messageHeader;
    }

    /**
     * Function to generate i2b2 message header message number
     *
     * @return String
     */
    protected String generateMessageId() {
        StringWriter strWriter = new StringWriter();
        for (int i = 0; i < 20; i++) {
            int num = getValidAcsiiValue();
            strWriter.append((char) num);
        }
        return strWriter.toString();
    }

    /**
     * Function to generate random number used in message number
     *
     * @return int
     */
    private int getValidAcsiiValue() {
        int number = 48;
        while (true) {
            SecureRandom random = new SecureRandom();

            number = 48 + (int) Math.round(random.nextDouble() * 74);
            if ((number > 47 && number < 58) || (number > 64 && number < 91)
                    || (number > 96 && number < 123)) {
                break;
            }
        }
        return number;
    }

    /**
     * Function to build Request message type
     *
     * @param messageHeader MessageHeader object
     * @param reqHeader RequestHeader object
     * @param bodyType BodyType object
     * @return RequestMessageType object
     */
    public RequestMessageType getRequestMessageType(MessageHeaderType messageHeader,
            RequestHeaderType reqHeader, BodyType bodyType) {
        RequestMessageType reqMsgType = new RequestMessageType();
        reqMsgType.setMessageHeader(messageHeader);
        reqMsgType.setMessageBody(bodyType);
        reqMsgType.setRequestHeader(reqHeader);
        return reqMsgType;
    }

}
