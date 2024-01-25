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
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.delegate.GetProductsRequestHandler;
import edu.pitt.dbmi.i2b2.ontologystore.delegate.ProductActionsRequestHandler;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDisableService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDownloadService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyInstallService;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Oct 6, 2022 10:07:19 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyStoreService extends AbstractWebService {

    private static final Log LOGGER = LogFactory.getLog(OntologyStoreService.class);

    private final PmDBAccess pmDBAccess;
    private final OntologyFileService ontologyFileService;
    private final OntologyDownloadService ontologyDownloadService;
    private final OntologyInstallService ontologyInstallService;
    private final OntologyDisableService ontologyDisableService;

    @Autowired
    public OntologyStoreService(
            PmDBAccess pmDBAccess,
            OntologyFileService ontologyFileService,
            OntologyDownloadService ontologyDownloadService,
            OntologyInstallService ontologyInstallService,
            OntologyDisableService ontologyDisableService) {
        this.pmDBAccess = pmDBAccess;
        this.ontologyFileService = ontologyFileService;
        this.ontologyDownloadService = ontologyDownloadService;
        this.ontologyInstallService = ontologyInstallService;
        this.ontologyDisableService = ontologyDisableService;
    }

    public OMElement getProducts(OMElement req) throws XMLStreamException, I2B2Exception {
        if (req == null) {
            return getNullRequestResponse();
        }

        ResponseDataMessage responseDataMsg = new ResponseDataMessage(req.toString());

        long waitTime = 0;
        if ((responseDataMsg.getRequestMessageType() != null) && (responseDataMsg.getRequestMessageType().getRequestHeader() != null)) {
            waitTime = responseDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
        }

        return execute(new GetProductsRequestHandler(responseDataMsg, ontologyFileService, pmDBAccess), waitTime);
    }

    public OMElement getProductActions(OMElement req) throws XMLStreamException, I2B2Exception {
        if (req == null) {
            return getNullRequestResponse();
        }

        ProductActionDataMessage productActionDataMsg = new ProductActionDataMessage(req.toString());

        long waitTime = 0;
        if ((productActionDataMsg.getRequestMessageType() != null) && (productActionDataMsg.getRequestMessageType().getRequestHeader() != null)) {
            waitTime = productActionDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
        }

        return execute(
                new ProductActionsRequestHandler(productActionDataMsg, ontologyDownloadService, ontologyInstallService, ontologyDisableService, pmDBAccess),
                waitTime);
    }

}
