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
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionsType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyActionService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import edu.pitt.dbmi.i2b2.ontologystore.ws.ProductActionDataMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 20, 2022 3:29:04 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class ProductActionsRequestHandler extends RequestHandler {

    private static final Log LOGGER = LogFactory.getLog(ProductActionsRequestHandler.class);

    private final ProductActionDataMessage productActionDataMsg;
    private final OntologyFileService ontologyFileService;
    private final OntologyActionService ontologyActionService;

    public ProductActionsRequestHandler(ProductActionDataMessage productActionDataMsg, OntologyFileService ontologyFileService, OntologyActionService ontologyActionService, PmDBAccess pmDBAccess) {
        super(pmDBAccess);
        this.productActionDataMsg = productActionDataMsg;
        this.ontologyFileService = ontologyFileService;
        this.ontologyActionService = ontologyActionService;
    }

    @Override
    public String execute() throws I2B2Exception {
        // authorization check
        MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(productActionDataMsg.getMessageHeaderType());
        ConfigureType configureType = getConfigureType(messageHeader);
        if (isInvalidUser(configureType, messageHeader)) {
            return createInvalidUserResponse(messageHeader);
        }
        if (isNotAdmin(configureType)) {
            return createNotAdminResponse(messageHeader);
        }

        // get requested actions from the client
        List<ProductActionType> requestedActions = getRequestActions();
        if (requestedActions.isEmpty()) {
            return createResponse(messageHeader, new ProductActionsType());
        }

        // get all available ontologies from the cloud
        Map<String, ProductItem> availableProducts = ontologyFileService.getProductItems(getProductListUrl());
        if (availableProducts.isEmpty()) {
            return createResponse(messageHeader, new ProductActionsType());
        }

        // keep the requested actions to products that are in the available from the cloud
        ProductActionsType reducedRequestActions = new ProductActionsType();
        requestedActions.stream()
                .filter(e -> availableProducts.containsKey(e.getId()))
                .forEach(reducedRequestActions.getProductAction()::add);

        ontologyActionService.executeActions(availableProducts, reducedRequestActions.getProductAction(), getDownloadDirectory(configureType));

        return createResponse(messageHeader, reducedRequestActions);
    }

    private String createResponse(MessageHeaderType messageHeader, ProductActionsType productActionsType) throws I2B2Exception {
        ResponseMessageType responseMessageType = MessageFactory
                .buildResponseProductActionsType(messageHeader, productActionsType);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

    private List<ProductActionType> getRequestActions() throws I2B2Exception {
        List<ProductActionType> requestedActions = new LinkedList<>();

        try {
            ProductActionsType productsType = productActionDataMsg.getProductActionsType();
            requestedActions.addAll(productsType.getProductAction());
        } catch (JAXBUtilException exception) {
            LOGGER.error("Error setting up ProductActionsRequestHandler");
            throw new I2B2Exception("ProductActionsType not configured");
        }

        return requestedActions;
    }

}
