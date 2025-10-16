package edu.pitt.dbmi.i2b2.ontologystore.util;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.i2b2message.RequestMessageType;
import jakarta.xml.bind.JAXBElement;
import org.junit.Test;

/**
 *
 * Sep 29, 2025 5:05:30 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class OntologyStoreJAXBUtilTest {

    private final String requestXml = """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProducts</redirect_url>
        </proxy>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>i2b2 Ontology</application_name>
            <application_version>1.8.2</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>Ontology Cell</application_name>
            <application_version>1.8.2</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2025-09-29T16:57:20.995-04:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>demo</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <message_num>c78lh8oL4Ht28659c3QEf</message_num>
            <instance_num>0</instance_num>
        </message_control_id>
        <processing_id>
            <processing_id>P</processing_id>
            <processing_mode>I</processing_mode>
        </processing_id>
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>
        <application_acknowledgement_type>AL</application_acknowledgement_type>
        <country_code>US</country_code>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:getProducts/>
    </message_body>
</ns3:request>
""";

    /**
     * Test of getJaxbUtil method, of class OntologyStoreJAXBUtil.
     *
     * @throws JAXBUtilException
     */
    @Test
    public void testGetJaxbUtil() throws JAXBUtilException {
        JAXBElement jaxbElement = OntologyStoreJAXBUtil.getJaxbUtil().unMashallFromString(requestXml);
        RequestMessageType requestMessageType = (RequestMessageType) jaxbElement.getValue();
    }

}
