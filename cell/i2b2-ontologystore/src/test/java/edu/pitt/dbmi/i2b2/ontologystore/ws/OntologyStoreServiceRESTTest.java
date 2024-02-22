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

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.ResponseMessageType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductsType;
import edu.pitt.dbmi.i2b2.ontologystore.util.OntologyStoreJAXBUtil;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import junit.framework.JUnit4TestAdapter;
import org.apache.axiom.om.OMElement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * Feb 22, 2024 2:45:26 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyStoreServiceRESTTest extends OntologyStoreAxisAbstract {

    private static String testFileDir = "";

    private static String ontologyStoreTargetEPR = null;
    //	"http://127.0.0.1:8080/i2b2/services/OntologyStoreService/getServices";

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(OntologyStoreServiceRESTTest.class);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        String host = (System.getProperty("testhost") == null ? "http://127.0.0.1:9090/i2b2/services" : System.getProperty("testhost"));

        ontologyStoreTargetEPR = host + "/OntologyStoreService";
        testFileDir = "src/test/resources"; //System.getProperty("testfiledir");

        if ((testFileDir == null) || (testFileDir.trim().isEmpty())) {
            throw new Exception("Please provide test file directory info -Dtestfiledir");
        }
    }

    @Ignore
    @Test
    public void testGetProducts() throws IOException {
        String filename = testFileDir + "/get_products.xml";
        try {
            String requestString = getQueryString(filename);
            OMElement requestElement = convertStringToOMElement(requestString);
            OMElement responseElement = getServiceClient(ontologyStoreTargetEPR + "/getProducts").sendReceive(requestElement);
            JAXBElement responseJaxb = OntologyStoreJAXBUtil.getJaxbUtil().unMashallFromString(responseElement.toString());

            JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
            ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
            ProductsType productsType = (ProductsType) helper.getObjectByClass(r.getMessageBody().getAny(), ProductsType.class);
            Assert.assertNotNull(productsType);
            Assert.assertFalse(productsType.getProduct().isEmpty());
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

}
