# i2b2-ontologystore

An i2b2 cell for downloading ontologies onto the server and for installing ontologies into i2b2 database.

## Building the Cell

### Prerequisites

The following software and tools are required:

- OpenJDK 8 or [Oracle JDK 8](https://www.oracle.com/java/technologies/downloads/#java8)
- [Apache Maven 3.x.x](https://maven.apache.org/download.cgi)

The following third-party Maven dependencies are required:

- [i2b2-server-common](https://github.com/kvb2univpitt/i2b2-server-common)

### Compiling the Code

1. Install the 3rd-party dependency [i2b2-server-common](https://github.com/kvb2univpitt/i2b2-server-common).

2. Open up a terminal in the folder ```ontology-store/cell/i2b2-ontologystore``` and execute the following command:

    ```
    mvn clean package
    ```

### Installing the Cell

#### Configuring the Cell

Create a folder called ```.spring``` in your home directory.  Create a file called **ontologystore.properties** in the ```.spring``` directory with the following content:

```properties
ontology.dir.download=

aws.s3.json.product.list=https://ontology-store.s3.amazonaws.com/product-list.json

# datasources
spring.hive.datasource.jndi-name=java:/OntologyBootStrapDS
spring.pm.datasource.jndi-name=java:/PMBootStrapDS
```

Set the value for the attribute ***ontology.dir.download*** with the location to where the ontologies will be downloaded to on the server.

> Usually, a user called ***wildfly*** is created for running Wildfly server in Linux.  The path of **ontologystore.properties** file would be **/home/wildfly/.spring/ontologystore.properties**

#### Copy the File to Wildfly

1. Stop Wildfly.
2. Copy the JAR file **OntologyStore.jar** from the directory ```ontology-store/cell/i2b2-ontologystore/target``` to the folder ```WEB-INF/lib``` inside the WAR file **i2b2.war**.
3. Copy the AAR file **OntologyStore.aar** from the directory ```ontology-store/cell/i2b2-ontologystore/target``` to the folder ```WEB-INF/services``` inside the WAR file **i2b2.war**.
4. Restart Wildfly.

## Calling SOAP API

The following instructions assume i2b2 hives is deployed on Wildfly server with domain name ***localhost*** on port ***9090*** with the following credentials:

| Username | Password | Role  | Domain   | Project ID |
|----------|----------|-------|----------|------------|
| demo     | demouser | user  | i2b2demo | Demo       |
| i2b2     | demouser | admin | i2b2demo | Demo       |

### Getting a List of Ontologies

To get a list of ontologies, make the following SOAP call to the endpoint **http://localhost:9090/i2b2/services/OntologyStoreService/getProducts**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProducts</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            <domain>i2b2demo</domain>
            <username>demo</username>
            <password>demouser</password>
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:getProducts></ns4:getProducts>
    </message_body>
</ns3:request>
```

Below is an example of the SOAP response:

```xml
<ns2:response xmlns:ns2="http://www.i2b2.org/xsd/hive/msg/1.1/"
              xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/"
              xmlns:ns3="http://www.i2b2.org/xsd/cell/pm/1.1/">
    <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>1.700</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2022-10-28T17:08:40.525-04:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>demo</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <instance_num>1</instance_num>
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
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:products>
            <product>
                <file_name>act_visit_details_v4.json</file_name>
                <title>ACT Visit Details Ontology</title>
                <version>V4</version>
                <owner>Pitt</owner>
                <type>Ontology Package</type>
                <terminologies>
                    <terminology>CPT4</terminology>
                    <terminology>LOINC</terminology>
                    <terminology>ICD10CM</terminology>
                    <terminology>UMLS</terminology>
                </terminologies>
                <include_network_package>false</include_network_package>
                <downloaded>false</downloaded>
                <installed>false</installed>
                <started>false</started>
                <failed>false</failed>
            </product>
            <product>
                <file_name>act_vital_signs_v4.json</file_name>
                <title>ACT Vital Signs Ontology</title>
                <version>V4</version>
                <owner>Pitt</owner>
                <type>Ontology Package</type>
                <terminologies>
                    <terminology>LOINC</terminology>
                </terminologies>
                <include_network_package>false</include_network_package>
                <downloaded>false</downloaded>
                <installed>false</installed>
                <started>false</started>
                <failed>false</failed>
            </product>
        </ns4:products>
    </message_body>
</ns2:response>
```

### Downloading and Installing Ontologies

Assume we have the following ontologies on the AWS Cloud:

| Ontology          | Title                      | Metadata File             |
|-------------------|----------------------------|---------------------------|
| ACT Visit Details | ACT Visit Details Ontology | act_visit_details_v4.json |
| ACT Vital Signs   | ACT Vital Signs Ontology   | act_vital_signs_v4.json   |


Assume we want to do the following:

- Download the ***ACT Visit Details*** ontology.
- Download and install the ***ACT Vital Signs*** ontology.

Make the following SOAP call to the endpoint **http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <title>ACT Visit Details Ontology</title>
                <key>act_visit_details_v4.json</key>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>false</install>
            </product_action>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>true</install>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

> Note that the user must have an **admin** role in order to download/install the ontologies.

Below is the SOAP response for the above SOAP call:

```xml
<ns2:response xmlns:ns2="http://www.i2b2.org/xsd/hive/msg/1.1/"
              xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/"
              xmlns:ns3="http://www.i2b2.org/xsd/cell/pm/1.1/">
    <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>1.700</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2022-10-28T17:30:04.048-04:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <instance_num>1</instance_num>
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
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:action_summaries>
            <action_summary>
                <title>ACT Visit Details Ontology</title>
                <action_type>Download</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Downloaded.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Download</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Downloaded.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Metadata Installed.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>CRC Data Installed.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

When making multiple identical SOAP call to download or install the ontologies the effect is the same as making a single call.  If you make the above call again, the ontologies will not be downloaded or installed again; you will the get following response:

```xml
<ns2:response xmlns:ns2="http://www.i2b2.org/xsd/hive/msg/1.1/"
              xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/"
              xmlns:ns3="http://www.i2b2.org/xsd/cell/pm/1.1/">
    <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>1.700</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2022-10-28T17:38:45.222-04:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <instance_num>1</instance_num>
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
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:action_summaries>
            <action_summary>
                <title>ACT Visit Details Ontology</title>
                <action_type>Download</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Already downloaded.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Download</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Already downloaded.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Already Installed.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

### Disabling/Enabling Ontologies

When an ontology is disabled, it is removed from the ***terms*** list in the i2b2 webclient.  Note that the ontology is still in the i2b2 database.

Assume we want to **disable** the ***ACT Vital Signs*** ontology, make the following SOAP call to the endpoint **http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

To **enable** the ***ACT Vital Signs*** ontology, make the same SOAP call above.

The following action will happen when ***disable_enable*** is set to **true**:

- The ontology will be ***disabled*** if it is currently ***enabled***.
- The ontology will be ***enabled*** if it is currently ***disabled***.

#### Disabling/Enabling Ontologies and Dowloading/Installing Ontologies In One Request

You can make a single to request to dowload/install ontologies and disable/enable ontologies in one SOAP call.

Assume we want to do the following:

- Install the ***ACT Visit Details*** ontology.
- Disable the ***ACT Vital Signs*** ontology.

Make the following SOAP call to the endpoint **http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions**:


```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <title>ACT Visit Details Ontology</title>
                <key>act_visit_details_v4.json</key>
                <include_network_package>false</include_network_package>
                <download>false</download>
                <install>true</install>
            </product_action>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

You should a response similar the following:

```xml
<ns2:response xmlns:ns2="http://www.i2b2.org/xsd/hive/msg/1.1/"
              xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/"
              xmlns:ns3="http://www.i2b2.org/xsd/cell/pm/1.1/">
    <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>1.700</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2022-11-18T14:24:51.672-05:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <instance_num>1</instance_num>
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
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:action_summaries>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Disable</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Disabled.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Visit Details Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Metadata Installed.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Visit Details Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>CRC Data Installed.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

#### Mixing Request to Disabling/Enabling Ontologies and Dowloading/Installing Ontologies

Assume you want to do the following:

- Download and install the ***ACT Vital Signs*** ontology.
- Disable the the ***ACT Vital Signs*** ontology.

Make the following SOAP call to the endpoint **http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>I2B2_URI</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>true</install>
            </product_action>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

The response will be similar to the following:

```xml
<ns2:response xmlns:ns2="http://www.i2b2.org/xsd/hive/msg/1.1/"
              xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/"
              xmlns:ns3="http://www.i2b2.org/xsd/cell/pm/1.1/">
    <message_header>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>1.700</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>2022-11-18T15:38:32.761-05:00</datetime_of_message>
        <security>
            <domain>i2b2demo</domain>
            <username>i2b2</username>
            <password>demouser</password>
        </security>
        <message_control_id>
            <instance_num>1</instance_num>
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
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:action_summaries>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Download</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Downloaded.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Metadata Installed.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Install</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>CRC Data Installed.</detail>
            </action_summary>
            <action_summary>
                <title>ACT Vital Signs Ontology</title>
                <action_type>Disable</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Disabled.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

Note that the order of the ```<product_action>``` does not matter.  The following request is still valid:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    ...
    <message_body>
        <ns4:product_actions>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <disable_enable>true</disable_enable>
            </product_action>
            <product_action>
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>true</install>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```
