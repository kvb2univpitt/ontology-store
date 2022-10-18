package edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Request
 * <?xml version="1.0" encoding="UTF-8"?>
 * <ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">
 *    <message_header>
 *        <proxy>
 *            <redirect_url>I2B2_URI</redirect_url>
 *        </proxy>
 *        <sending_application>
 *            <application_name>i2b2 Ontology Service</application_name>
 *            <application_version>1.7</application_version>
 *        </sending_application>
 *        <sending_facility>
 *            <facility_name>i2b2 Hive</facility_name>
 *        </sending_facility>
 *        <security>
 *            <domain>i2b2demo</domain>
 *            <username>demo</username>
 *            <password></password>
 *        </security>
 *        <project_id>Demo</project_id>
 *    </message_header>
 *    <request_header>
 *        <result_waittime_ms>180000</result_waittime_ms>
 *    </request_header>
 *    <message_body>
 *        <ns4:getProducts></ns4:getProducts>
 *    </message_body>
 * </ns3:request>
 * 
 * @author kvb2
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "helloWorld", propOrder = {
    "name"
})
public class HelloWorldType {

    private String name;

    public HelloWorldType() {
    }

    public HelloWorldType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
