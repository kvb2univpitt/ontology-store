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
package edu.pitt.dbmi.i2b2.ontologystore.delegate;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

/**
 *
 * Oct 10, 2022 12:40:11 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class RequestHandler {

    private static final Log LOGGER = LogFactory.getLog(RequestHandler.class);
    protected final Logger LOGGER_API = ESAPI.getLogger(RequestHandler.class);

    protected final PmDBAccess pmDBAccess;

    public RequestHandler(PmDBAccess pmDBAccess) {
        this.pmDBAccess = pmDBAccess;
    }

    public abstract String execute() throws I2B2Exception;

    protected boolean isInvalidUser(MessageHeaderType messageHeader) {
        return pmDBAccess.getRoleInfo(messageHeader) == null;
    }

    protected boolean isNotAdmin(MessageHeaderType messageHeader) throws I2B2Exception {
        return !pmDBAccess.isAdmin(messageHeader);
    }

    protected String createInvalidUserResponse(MessageHeaderType messageHeader) throws I2B2Exception {
        return createErrorResponse(messageHeader, "User was not validated.");
    }

    protected String createNotAdminResponse(MessageHeaderType messageHeader) throws I2B2Exception {
        return createErrorResponse(messageHeader, "User does not have administrator privileges.");
    }

    protected String createErrorResponse(MessageHeaderType messageHeader, String errorMessage) throws I2B2Exception {
        ResponseMessageType responseMessageType = MessageFactory.doBuildErrorResponse(messageHeader, errorMessage);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

}
