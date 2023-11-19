# i2b2-ontologystore

An i2b2 cell providing the following functionalities:

- Download the i2b2 ontologies from the cloud onto the server.
- Import the dowloaded ontologies into an i2b2 database.

The ontologies are provided by the community and are publicly available on the AWS cloud.

## Installing the OntologyStore Cell

### Prerequisites

#### Required Software and Tools:

- OpenJDK 8 or [Oracle JDK 8](https://www.oracle.com/java/technologies/downloads/#java8)
- [Apache Maven 3.x.x](https://maven.apache.org/download.cgi)

#### Required i2b2 Software

- i2b2 Core Server 1.7.13 Release.
- i2b2 Webclient 1.7.13 Release.

#### Required Permissions

- System administrator privileges is needed to install the OntologyStore plugin and to install the OntologyStore cell.
- i2b2 administrator privileges is needed to log into the i2b2 Administration Module to add new cell.

#### Required Third-party Maven Dependencies:

- [i2b2-server-common](https://github.com/kvb2univpitt/i2b2-server-common)

### Building the Cell

#### 1. Install the 3rd-party dependency [i2b2-server-common](https://github.com/kvb2univpitt/i2b2-server-common).

#### 2. Open up a terminal in the project directory ```ontology-store/cell/i2b2-ontologystore``` and execute the following Maven command to compile the code and package it in its distributable format:

```
mvn clean package
```

The output files of the build is located in the project folder ```ontology-store/cell/i2b2-ontologystore/target```.  The OntologyStore cell consists of following output files:

- OntologyStore.jar
- OntologyStore.aar

### Adding the Cell to the Hives

The instructions assume the following:

- The Wildfly directory on the server is ```/opt/wildfly```.
- The i2b2 webclient directory on the server is ```/var/www/html/webclient```
- The directory to download the ontology files is ```/home/wildfly/ontology```

#### 1. Stop the Wildfly server.

#### 2. Add the compiled modules to the i2b2 cell on the server.

- Copy the aar file OntologyStore.aar from the ontologystore_cell folder to the Wildfly directory ```/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/services```.

- Copy the jar file OntologyStore.jar ontologystore_cell folder to the Wildfly directory ```/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/lib```.

> Note that the i2b2.war in the Wildfly directory /opt/wildfly/standalone/deployments may be an actual WAR file instead of a directory. In this case, you will need to open up the i2b2.war file and add the OntologyStore.aar file to the ```WEB-INF/services``` folder and the OntologyStore.jar file to the ```WEB-INF/lib``` folder.

#### 3. Configure the i2b2 OntologyStore cell.

- Create a file called **ontologystore.properties** in the Wildfly configuration directory ```/opt/wildfly/standalone/configuration``` with the following content:

    ```
    ontology.dir.download=/home/wildfly/ontology
    
    aws.s3.json.product.list=https://ontology-store.s3.amazonaws.com/product-list.json

    # datasources
    spring.hive.datasource.jndi-name=java:/OntologyBootStrapDS
    spring.pm.datasource.jndi-name=java:/PMBootStrapDS
    ```

    > Remember to change the property value for the **ontology.dir.download** to the actual location where the ontology files should be downloaded to.
    
##### Moving the configuration file to different directory

If you prefer the **ontologystore.properties** to be in a different directory, change the value of the property **location** in the **applicationContext.xml** file located in the project directory ```ontology-store/cell/i2b2-ontologystore/src/main/resources```.

For an example, assume the **ontologystore.properties** is located in the directory ```/home/wildfly/i2b2```.  Copy the **ontologystore.properties** to the directory ```/home/wildfly/i2b2``` and change the value of the property **location** to the following in the **applicationContext.xml** file:

```xml
<!-- application.properties -->
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <!--<property name="location" value="file:${jboss.server.config.dir}/ontologystore.properties" />-->
    <property name="location" value="file:/home/wildfly/i2b2/ontologystore.properties" />
</bean>
```

#### 4. Start the Wildfly server.


The instructions assume the following:

- The i2b2 webclient directory on the server is ```/var/www/html/webclient```

### Adding the OntologyStore Cell module in the i2b2 Webclient.

#### 1.  Copy to the folder **ONTSTORE** from the project directory ```ontology-store/cell``` to the webclient directory ```/var/www/html/webclient/js-i2b2/cells```

#### 2. Add the following code to the array ***i2b2.hive.tempCellsList*** in the module loader configuration file **i2b2_loader.js** located in the directory ```/var/www/html/webclient/js-i2b2```:

```
{code: "ONTSTORE"},
```

For an example, the **i2b2_loader.js** file should look similar to this:

```js
i2b2.hive.tempCellsList = [
    {code: "PM",
        forceLoading: true 			// <----- this must be set to true for the PM cell!
    },
    {code: "ONT"},
    {code: "CRC"},
    {code: "WORK"},
    {code: "ONTSTORE"},
    ...
];
```

### Registering the Cell in the Webclient

The i2b2 webclient needs to know about the OntologyStore cell.  Assume the i2b2 hive is deployed on a Wildfly server with the domain name ***localhost*** on port ***9090***, the following cell information will be added in the i2b2 Administration Module:

| Field        | Description                                                           | Value                                                     |
|--------------|-----------------------------------------------------------------------|-----------------------------------------------------------|
| Cell ID      | A unique identifier for the cell.                                     | ONTSTORE                                                  |
| Cell Name    | The name of the cell.                                                 | OntologyStore Cell                                        |
| Cell URL     | The url contains the IP or domain name for where the cell is located. | http://localhost:9090/i2b2/services/OntologyStoreService/ |
| Project Path |                                                                       | /                                                         |
| Method       | The method of communication.                                          | REST                                                      |

#### 1. Log into the i2b2 Administration Module.

#### 2. In the Navigation panel, click on ***Manage Cells***.

![Manged Cell](img/managed_cell.png)

#### 3. In the Manage Cells page click on Add New Cell.

![Add Cell](img/add_cell.png)

#### 4. Enter the above cell information and click on the "Save" button:

![Save Cell](img/save_cell.png)

> Note that the URL must end with a foward slash (**/**).

#### 5. The cell will be added to the list of cells on the Manage Cells page.  In the Navigation panel click on Manage Cells to refresh the hierarchical tree and display the new cell:

![Refresh Cell List](img/refresh_managed_cell.png)

## Making SOAP Calls to the Cell

### Example Setup

Assume the i2b2 hive containing the OntologyStore cell is deployed on a Wildfly server with the following configurations:

| Domain    | Port |
|-----------|------|
| localhost | 9090 |

Assume the i2b2 database contains the following user credentials:

| Username | Password | Role  | Domain   | Project ID |
|----------|----------|-------|----------|------------|
| demo     | demouser | user  | i2b2demo | Demo       |
| i2b2     | demouser | admin | i2b2demo | Demo       |

Assume the AWS Cloud has the following ontologies:

| Ontology          | Title                      | Metadata File             |
|-------------------|----------------------------|---------------------------|
| ACT Visit Details | ACT Visit Details Ontology | act_visit_details_v4.json |
| ACT Vital Signs   | ACT Vital Signs Ontology   | act_vital_signs_v4.json   |

### SOAP Request Template

Below is the basic template for making a SOAP call to the OntologyStore cell:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>*** SOAP endpoint goes here ***</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 OntologyStore Service</application_name>
            <application_version>1.7</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <security>
            *** User credentials go here. ***
        </security>
        <project_id>Demo</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        *** Request payload goes here. ***
    </message_body>
</ns3:request>
```

The SOAP endpoint is enclosed in the ```<redirect_url></redirect_url>``` tags.  The user credentials is enclosed in the ```<security></security>``` tags.  The request payload is enclosed in the ```<message_body></message_body>``` tags.

### Getting a List of Ontologies

#### Making the Call Manually

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

#### Making the Call Using the Javascript API (Webclient)

Below is the Javascript code to get a list of ontologies using the i2b2 webclient:

```javascript
// creating a callback
var scopedCallback = new i2b2_scopedCallback();
scopedCallback.callback = function (results) {
    if (results.error) {
        // handle error
    } else {
        // handle successful call
    }
};

// making a SOAP call
i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, scopedCallback);
```

### Downloading, Installing, and Disable/Enable Ontologies

The request payload contains a list of actions enclosed in the ```<ns4:product_actions></ns4:product_actions>``` tags.  Each action is enclosed in the ```<product_action></product_action>``` tags.

> Administrator privileges are required to dowloand, install, and disable/enable ontologies.

#### Product Action Parameters

| Parameter               | Description                                              | Requirement                                               | Default Value |
|-------------------------|----------------------------------------------------------|-----------------------------------------------------------|---------------|
| title                   | Ontology title.                                          | <span style="color:red;font-weight:bold;">Required</span> |               |
| key                     | Ontology metadata file (JSON).                           | <span style="color:red;font-weight:bold;">Required</span> |               |
| include_network_package | Indicates additional file for Shrine to be downloaded    | Optional                                                  | false         |
| download                | Indicates the ontology to be downloaded.                 | Optional                                                  | false         |
| install                 | Indicates the ontology to be installed.                  | Optional                                                  | false         |
| disable_enable          | Indicates the ontology to be either disabled or enabled. | Optional                                                  | false         |


#### Downloading and Installing

Assume we want to do the following:

- Download the ***ACT Visit Details*** ontology.
- Download and install the ***ACT Vital Signs*** ontology.

##### Making the Call Manually

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

We can omit the ```<install></install>``` tags and the ```<disable_enable></disable_enable>``` tags in the download request, since they are set to false by default.

> Note that the user has the **administrator** privileges.

Below is an example of the SOAP response for the above call:

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

##### Making the Call Using Javascript API (Webclient)

```javascript
// creating the payload
var payload = '<product_action>\n' +
'    <title>ACT Visit Details Ontology</title>\n' +
'    <key>act_visit_details_v4.json</key>\n' +
'    <include_network_package>false</include_network_package>\n' +
'    <download>true</download>\n' +
'</product_action>\n' +
'<product_action>\n' +
'    <title>ACT Vital Signs Ontology</title>\n' +
'    <key>act_vital_signs_v4.json</key>\n' +
'    <include_network_package>false</include_network_package>\n' +
'    <download>true</download>\n' +
'    <install>true</install>\n' +
'</product_action>';

// creating the SOAP options
let options = {
    version: i2b2.ClientVersion,
    products_str_xml: payload
};

// creating a callback
var scopedCallback = new i2b2_scopedCallback();
scopedCallback.callback = function (results) {
    if (results.error) {
        // handle error
    } else {
        // handle successful call
    }
};

// making a SOAP call
i2b2.ONTSTORE.ajax.PerformProductActions("OntologyStore Plugin", options, scopedCallback);
```

#### Idempotence

Making multiple identical SOAP calls to ***download*** and/or  ***install*** the ontologies have no effect on the ontologies.  If we make another SOAP call to the above request, we would get the following SOAP response:

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

#### Disabling Ontologies

When an ontology is disbled, it is removed from the ***Terms*** list in the i2b2 webclient.  The ontology gets put back on the ***Terms*** list when it is enabled.  Disabling ontologies can be useful if we want to prevent the user from using certain ontologies.  Note that only **installed ontologies** can be disabled or enabled.


Assume we want to disable the installed ***ACT Vital Signs*** ontology.

##### Making the Call Manually

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
                <title>ACT Vital Signs Ontology</title>
                <key>act_vital_signs_v4.json</key>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

Below is an example of the SOAP response for the above call:

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
        <datetime_of_message>2022-11-23T21:49:30.611-05:00</datetime_of_message>
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
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

##### Making the Call Using Javascript API (Webclient)

```javascript
// creating the payload
var payload = '<product_action>\n' +
'    <title>ACT Vital Signs Ontology</title>\n' +
'    <key>act_vital_signs_v4.json</key>\n' +
'    <disable_enable>true</disable_enable>\n' +
'</product_action>';

// creating the SOAP options
let options = {
    version: i2b2.ClientVersion,
    products_str_xml: payload
};

// creating a callback
var scopedCallback = new i2b2_scopedCallback();
scopedCallback.callback = function (results) {
    if (results.error) {
        // handle error
    } else {
        // handle successful call
    }
};

// making a SOAP call
i2b2.ONTSTORE.ajax.PerformProductActions("OntologyStore Plugin", options, scopedCallback);
```

#### Enabling Ontologies

Assume the ***ACT Vital Signs*** ontology was previously disabled.  To enable the ontology, we would make the identical SOAP called as above to the same endpoint:

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

Below is an example of the SOAP response for the above call:

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
        <datetime_of_message>2022-11-23T22:08:27.263-05:00</datetime_of_message>
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
                <action_type>Enable</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Enabled.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

Note that the SOAP call to disable or enable ontology is ***not Idempotent***.  If the ontology is enabled, calling the above SOAP call will disable it and vice-versa.

#### Downloading, Installing and Disabling Ontologies

You can download the ontology, install the ontology, and disable the ontology in one request.

Assume we want to download, install, and disable ***ACT Visit Details*** ontology.

##### Making the Call Manually

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
                <install>true</install>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

Below is an example of the SOAP response for the above call:

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
        <datetime_of_message>2022-11-23T22:24:39.628-05:00</datetime_of_message>
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
            <action_summary>
                <title>ACT Visit Details Ontology</title>
                <action_type>Disable</action_type>
                <in_progress>false</in_progress>
                <success>true</success>
                <detail>Disabled.</detail>
            </action_summary>
        </ns4:action_summaries>
    </message_body>
</ns2:response>
```

##### Making the Call Using Javascript API (Webclient)

```javascript
// creating the payload
var payload = '<product_action>\n' +
'    <title>ACT Visit Details Ontology</title>\n' +
'    <key>act_visit_details_v4.json</key>\n' +
'    <include_network_package>false</include_network_package>\n' +
'    <download>true</download>\n' +
'    <install>true</install>\n' +
'    <disable_enable>true</disable_enable>\n' +
'</product_action>';

// creating the SOAP options
let options = {
    version: i2b2.ClientVersion,
    products_str_xml: payload
};

// creating a callback
var scopedCallback = new i2b2_scopedCallback();
scopedCallback.callback = function (results) {
    if (results.error) {
        // handle error
    } else {
        // handle successful call
    }
};

// making a SOAP call
i2b2.ONTSTORE.ajax.PerformProductActions("OntologyStore Plugin", options, scopedCallback);
```
