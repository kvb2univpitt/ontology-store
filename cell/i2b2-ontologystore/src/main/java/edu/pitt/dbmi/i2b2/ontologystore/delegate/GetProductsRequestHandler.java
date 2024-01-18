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
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductsType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import edu.pitt.dbmi.i2b2.ontologystore.ws.ResponseDataMessage;

/**
 *
 * Oct 10, 2022 7:51:57 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetProductsRequestHandler extends RequestHandler {

    private final ResponseDataMessage responseDataMessage;
    private final OntologyFileService ontologyFileService;

    public GetProductsRequestHandler(
            ResponseDataMessage responseDataMessage,
            OntologyFileService ontologyFileService,
            PmDBAccess pmDBAccess) {
        super(pmDBAccess);
        this.responseDataMessage = responseDataMessage;
        this.ontologyFileService = ontologyFileService;
    }

    @Override
    public String execute() throws I2B2Exception {
        MessageHeaderType messageHeader = MessageFactory
                .createResponseMessageHeader(responseDataMessage.getMessageHeaderType());
        if (isInvalidUser(messageHeader)) {
            return createInvalidUserResponse(messageHeader);
        }

        ProductsType productsType = new ProductsType();
        productsType.getProduct().addAll(ontologyFileService.getAvailableProducts());

        ResponseMessageType responseMessageType = MessageFactory
                .buildGetProductsResponse(messageHeader, productsType);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

}
