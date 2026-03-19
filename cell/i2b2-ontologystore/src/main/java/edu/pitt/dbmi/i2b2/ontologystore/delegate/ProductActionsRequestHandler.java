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
import edu.pitt.dbmi.i2b2.ontologystore.InstallationException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.pm.ConfigureType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionsType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import edu.pitt.dbmi.i2b2.ontologystore.service.AsyncActionService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDisableService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDownloadService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyInstallService;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import edu.pitt.dbmi.i2b2.ontologystore.ws.ProductActionDataMessage;
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
    private final OntologyDownloadService ontologyDownloadService;
    private final OntologyInstallService ontologyInstallService;
    private final OntologyDisableService ontologyDisableService;
    private final AsyncActionService asyncActionService;

    public ProductActionsRequestHandler(
            ProductActionDataMessage productActionDataMsg,
            OntologyFileService ontologyFileService,
            OntologyDownloadService ontologyDownloadService,
            OntologyInstallService ontologyInstallService,
            OntologyDisableService ontologyDisableService,
            AsyncActionService asyncActionService,
            PmDBAccess pmDBAccess) {
        super(pmDBAccess);
        this.productActionDataMsg = productActionDataMsg;
        this.ontologyFileService = ontologyFileService;
        this.ontologyDownloadService = ontologyDownloadService;
        this.ontologyInstallService = ontologyInstallService;
        this.ontologyDisableService = ontologyDisableService;
        this.asyncActionService = asyncActionService;
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

        ProductActionsType productActions = new ProductActionsType();
        try {
            ProductActionsType productsType = productActionDataMsg.getProductActionsType();
            productActions.getProductAction().addAll(productsType.getProductAction());
        } catch (JAXBUtilException exception) {
            LOGGER.error("Error setting up ProductActionsRequestHandler");
            throw new I2B2Exception("ProductActionsType not configured");
        }

        // get properties from database
        String productListUrl = getProductListUrl();
        String downloadDirectory = getDownloadDirectory(configureType);

        // get data from request
        String projectId = messageHeader.getProjectId();
        List<ProductActionType> actions = productActions.getProductAction();

        runEnableDisableTasks(productListUrl, downloadDirectory, projectId, actions);
        runDownloadInstallTasks(productListUrl, downloadDirectory, projectId, actions);

        ResponseMessageType responseMessageType = MessageFactory
                .buildProductActionsResponse(messageHeader, productActions);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

    private void runDownloadInstallTasks(String productListUrl, String downloadDirectory, String projectId, List<ProductActionType> actions) throws I2B2Exception {
        Map<String, ProductItem> products = ontologyFileService.getUniqueProductItems(productListUrl);
        List<ProductItem> productItemsToDownload = ontologyDownloadService.getValidProductsToDownload(downloadDirectory, actions, products);
        List<ProductItem> productItemsToInstall = ontologyInstallService.getValidProductsToInstall(downloadDirectory, actions, products);

        asyncActionService.performActions(projectId, downloadDirectory, productItemsToDownload, productItemsToInstall);
    }

    private void runEnableDisableTasks(String productListUrl, String downloadDirectory, String projectId, List<ProductActionType> actions) throws I2B2Exception {
        try {
            ontologyDisableService.performDisableEnable(downloadDirectory, projectId, productListUrl, actions);
        } catch (InstallationException exception) {
            throw new I2B2Exception(exception.getMessage());
        }
    }

}
