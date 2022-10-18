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
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.BodyType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.MessageHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestHeaderType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.util.OntologyStoreJAXBUtil;
import javax.xml.bind.JAXBElement;

/**
 *
 * Oct 9, 2022 2:33:30 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class RequestDataMessage {

    private final RequestMessageType requestMessageType;

    public RequestDataMessage(String requestMessageXml) throws I2B2Exception {
        try {
            JAXBElement jaxbElement = OntologyStoreJAXBUtil.getJaxbUtil().unMashallFromString(requestMessageXml);
            this.requestMessageType = (RequestMessageType) jaxbElement.getValue();
        } catch (JAXBUtilException exception) {
            throw new I2B2Exception("Unable to unmarshal request to RequestMessageType.", exception);
        }
    }

    public RequestMessageType getRequestMessageType() {
        return requestMessageType;
    }

    public MessageHeaderType getMessageHeaderType() {
        return requestMessageType.getMessageHeader();
    }

    public RequestHeaderType getRequestHeaderType() {
        return requestMessageType.getRequestHeader();
    }

    public BodyType getBodyType() {
        return requestMessageType.getMessageBody();
    }

}
