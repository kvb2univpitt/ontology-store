/*
 * Copyright (C) 2024 University of Pittsburgh.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

/**
 *
 * Feb 22, 2024 2:52:04 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public abstract class OntologyStoreAxisAbstract {

    public static String getQueryString(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    public static ServiceClient getServiceClient(String serviceUrl) throws Exception {
        Options options = new Options();
        options.setTo(new EndpointReference(serviceUrl));
        options.setTimeOutInMilliSeconds(2700000);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);

        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);

        return sender;
    }

    public static OMElement convertStringToOMElement(String requestXmlString) throws Exception {
        return edu.harvard.i2b2.common.util.axis2.ServiceClient.getPayLoad(requestXmlString);
    }

}
