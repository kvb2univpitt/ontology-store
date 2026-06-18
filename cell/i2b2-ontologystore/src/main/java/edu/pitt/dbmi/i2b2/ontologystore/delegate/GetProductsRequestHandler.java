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
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.GetProductsType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductsType;
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.ws.GetProductsDataMessage;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 10, 2022 7:51:57 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetProductsRequestHandler extends RequestHandler {

    private static final Log LOGGER = LogFactory.getLog(GetProductsRequestHandler.class);

    private final GetProductsDataMessage getProductsDataMessage;
    private final OntologyFileService ontologyFileService;

    public GetProductsRequestHandler(
            GetProductsDataMessage getProductsDataMessage,
            OntologyFileService ontologyFileService,
            PmDBAccess pmDBAccess,
            HiveDBAccess hiveDBAccess) {
        super(pmDBAccess, hiveDBAccess);
        this.getProductsDataMessage = getProductsDataMessage;
        this.ontologyFileService = ontologyFileService;
    }

    @Override
    public String execute() throws I2B2Exception {
        // authorization check
        MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getProductsDataMessage.getMessageHeaderType());
        ConfigureType configureType = getConfigureType(messageHeader);
        if (isInvalidUser(configureType, messageHeader)) {
            return createInvalidUserResponse(messageHeader);
        }
        if (isNotAdmin(configureType)) {
            return createNotAdminResponse(messageHeader);
        }

        GetProductsType getProductsType = new GetProductsType();
        try {
            GetProductsType requestData = getProductsDataMessage.getGetProductsType();
            getProductsType.setProjectId(requestData.getProjectId());
        } catch (JAXBUtilException exception) {
            LOGGER.error("Error setting up GetProductsRequestHandler");
            throw new I2B2Exception("GetProductsType not configured");
        }

        // get properties
        String productListUrl = getProductListUrl();
        String downloadDirectory = getDownloadDirectory();
        String projectId = getProductsType.getProjectId();

        ProductsType productsType = new ProductsType();
        productsType.getProduct().addAll(ontologyFileService.getAvailableProducts(projectId, downloadDirectory, productListUrl));

        ResponseMessageType responseMessageType = MessageFactory
                .buildGetProductsResponse(messageHeader, productsType);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

}
