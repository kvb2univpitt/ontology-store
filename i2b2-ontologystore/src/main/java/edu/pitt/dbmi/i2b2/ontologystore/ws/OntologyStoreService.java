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
import edu.pitt.dbmi.i2b2.ontologystore.db.HiveDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.db.PmDBAccess;
import edu.pitt.dbmi.i2b2.ontologystore.delegate.GetProductsRequestHandler;
import edu.pitt.dbmi.i2b2.ontologystore.service.AmazonS3Service;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Oct 6, 2022 10:07:19 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyStoreService extends AbstractWebService {

    private static final Log LOGGER = LogFactory.getLog(OntologyStoreService.class);

    private final PmDBAccess pmDBAccess;
    private final HiveDBAccess hiveDBAccess;
    private final AmazonS3Service amazonS3Service;

    public OntologyStoreService(PmDBAccess pmDBAccess, HiveDBAccess hiveDBAccess, AmazonS3Service amazonS3Service) {
        this.pmDBAccess = pmDBAccess;
        this.hiveDBAccess = hiveDBAccess;
        this.amazonS3Service = amazonS3Service;
    }

    public OMElement getProducts(OMElement req) throws XMLStreamException, I2B2Exception {
        if (req == null) {
            return getNullRequestResponse();
        }

        String xmlRequest = req.toString();
        ResponseDataMessage getProductsDataMessage = new ResponseDataMessage(xmlRequest);

        return execute(new GetProductsRequestHandler(getProductsDataMessage, amazonS3Service, pmDBAccess), 5000);
    }

}
