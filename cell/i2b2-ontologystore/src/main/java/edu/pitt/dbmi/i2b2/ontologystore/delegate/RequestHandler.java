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
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;

/**
 *
 * Oct 10, 2022 12:40:11 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class RequestHandler {

    protected final PmDBAccess pmDBAccess;

    public RequestHandler(PmDBAccess pmDBAccess) {
        this.pmDBAccess = pmDBAccess;
    }

    public abstract String execute() throws I2B2Exception;

    protected boolean isInvalidUser(ConfigureType configureType, MessageHeaderType header) {
        return pmDBAccess.getRoleInfo(configureType, header) == null;
    }

    protected boolean isNotAdmin(ConfigureType configureType) {
        return !pmDBAccess.isAdmin(configureType);
    }

    public String getProductListUrl() throws I2B2Exception {
        return pmDBAccess.getOntStoreProductListUrl();
    }

    public String getDownloadDirectory(ConfigureType configureType) throws I2B2Exception {
        return pmDBAccess.getDownloadDirectory(configureType);
    }

    public ConfigureType getConfigureType(MessageHeaderType header) {
        return pmDBAccess.getConfigureType(header);
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
