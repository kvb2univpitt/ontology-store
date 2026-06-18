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
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.GetProductsType;

/**
 *
 * Jun 16, 2026 9:23:24 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetProductsDataMessage extends RequestDataMessage {

    public GetProductsDataMessage(String requestMessageXml) throws I2B2Exception {
        super(requestMessageXml);
    }

    public GetProductsType getGetProductsType() throws JAXBUtilException {
        BodyType bodyType = getBodyType();

        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();

        return (GetProductsType) helper.getObjectByClass(bodyType.getAny(), GetProductsType.class);
    }

}
