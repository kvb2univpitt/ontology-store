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
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummariesType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionsType;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDownloadService;
import edu.pitt.dbmi.i2b2.ontologystore.ws.MessageFactory;
import edu.pitt.dbmi.i2b2.ontologystore.ws.ProductActionDataMessage;
import java.util.LinkedList;
import java.util.List;
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
    private final OntologyDownloadService ontologyDownloadService;

    public ProductActionsRequestHandler(
            ProductActionDataMessage productActionDataMsg,
            OntologyDownloadService ontologyDownloadService,
            PmDBAccess pmDBAccess) {
        super(pmDBAccess);
        this.productActionDataMsg = productActionDataMsg;
        this.ontologyDownloadService = ontologyDownloadService;
    }

    @Override
    public String execute() throws I2B2Exception {
        MessageHeaderType messageHeader = MessageFactory
                .createResponseMessageHeader(productActionDataMsg.getMessageHeaderType());
        if (isInvalidUser(messageHeader)) {
            return createInvalidUserResponse(messageHeader);
        }
        if (isNotAdmin(messageHeader)) {
            return createNotAdminResponse(messageHeader);
        }

        List<ProductActionType> actions = new LinkedList<>();
        try {
            ProductActionsType productsType = productActionDataMsg.getProductActionsType();
            actions.addAll(productsType.getProductAction());
        } catch (JAXBUtilException exception) {
            LOGGER.error("Error setting up ProductActionsRequestHandler");
            throw new I2B2Exception("ProductActionsType not configured");
        }

        ActionSummariesType actionSummariesType = new ActionSummariesType();
        List<ActionSummaryType> summaries = actionSummariesType.getActionSummary();
//        try {
//            ontologyDownloadService.performDownload(actions, summaries);
////            installService.performInstallation(messageHeader.getProjectId(), actions, summaries);
//
////            disableService.performDisableEnable(messageHeader.getProjectId(), actions, summaries);
//        } catch (InstallationException exception) {
//            throw new I2B2Exception(exception.getMessage());
//        }
        ontologyDownloadService.performDownload(actions, summaries);

        ResponseMessageType responseMessageType = MessageFactory
                .buildGetActionSummariesResponse(messageHeader, actionSummariesType);

        return MessageFactory.convertToXMLString(responseMessageType);
    }

}
