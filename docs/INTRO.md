# OntologyStore

OntologyStore is an administrative tool that enables i2b2 administrator to download ontologies hosted on the cloud and to import them into the i2b2 database.

OntologyStore consists of the following components:

- The OntologyStore cell - assists in fetching the list of ontologies from the cloud, download the
ontologies onto a storage location and installing the downloaded ontologies.

- The OntologyStore webclient plugin - assists in displaying the list of ontologies, selecting
ontologies to download and install, and hiding (disabled) installed ontologies.

<figure>
    <img src="../img/ontstore-plugin_2.svg" alt="OntologyStore Flow Diagram" />
    <figcaption><b>Figure 1: </b>OntologyStore Flow Diagram</figcaption>
</figure>

## Ontology

The ontology consists of the following tab-delimited (tsv) files.

- **Metadata**: A new metadata table will be created for importing the data. The filename will be used as the table name.
- **Concept Dimension**: A new concept dimension table will be created for importing the data. The filename suffixed with _cd will be used as the table name. To run queries, the data must be copied over to the main i2b2 ***CONCEPT_DIMENSION*** table.
- **Schemes**: Data will be directly imported into the i2b2 ***SCHEMES*** table.
- **Table Access**: Data will be directly imported into the i2b2 ***TABLE_ACCESS*** table.

### Ontology Package

The ontology package is a zip file containing the ontology files along with a JSON file called ***package.json***.

The package.json tell the OntologyStore cell which files are the metadata files, which files are the concept  dimension files, and etc.

```json
{
    "tableAccess": [
        "table_access/TABLE_ACCESS.tsv"
    ],
    "schemes": ["metadata/SCHEMES.tsv"],
    "breakdownPath": ["crc/QT_BREAKDOWN_PATH.tsv"],
    "conceptDimensions": [
        "crc/ACT_COVID_V4_CD.tsv",
        "crc/ACT_CPT4_PX_V4_CD.tsv",
        "crc/ACT_DEM_V4_CD.tsv"
    ],
    "domainOntologies": [
        "metadata/ACT_COVID_V4.tsv",
        "metadata/ACT_CPT4_PX_V4.tsv",
        "metadata/ACT_DEM_V4.tsv"
    ]
}
```
<figure>
    <figcaption><b>Figure 2: </b>An example of package.json</figcaption>
</figure>

### Ontology Product

The ontology product is a JSON object containing the description of the ontology and the URL to download the ontology package.

The ontology product contains the following information (attributes):

| Attribute      | Description                                                  | Required |
|----------------|--------------------------------------------------------------|----------|
| id             | A unique name for the ontology.                              | Yes      |
| title          | Full name of the ontology.                                   | Yes      |
| version        | The release version of the ontology.                         | Yes      |
| owner          | The author/creator of the ontology file.                     | Yes      |
| type           | The specific usage for the ontology.                         | Yes      |
| networkFiles   | An array of URLs that point to the network files for Shrine. | No       |
| terminologies  | The list of terminologies describing the ontology.           | No       |
| file           | The URL pointing to the ontology package file.               | Yes      |
| sha256Checksum | Sha256 checksum for the ontology package file.               | Yes      |

```json
{
    "id": "act_network_ontology_v4",
    "title": "ACT Network Ontology",
    "version": "V4",
    "owner": "Pitt",
    "type": "Network Ontology Package",
    "networkFiles": ["https://act-ontology-v4-test.s3.amazonaws.com/network_files/AdapterMappingV4.zip"],
    "terminologies": ["CPT4", "LOINC", "ICD10CM", "UMLS"],
    "file": "https://ontology-store-v2.s3.amazonaws.com/products/act_network_ontology_v4.zip",
    "sha256Checksum": "ee11f40630bae3fb87dac755c6ddbe7cd73594ada5d4991ebd559de27351f014"
}
```
<figure>
    <figcaption><b>Figure 3: </b>An example of an ontology product</figcaption>
</figure>


### Ontology Product List

The Ontology product list is a JSON object containing a list of the ontology products.  This is what the OntologyStore cell fetches to get the list of onotologies.

```json
{
    "products": [
        {
            "id": "act_covid_v4",
            "title": "ACT COVID-19 Ontology",
            "version": "V4",
            "owner": "Pitt",
            "type": "Ontology Package",
            "networkFiles": null,
            "terminologies": ["UMLS"],
            "file": "https://ontology-store-v2.s3.amazonaws.com/products/act_covid_v4.zip",
            "sha256Checksum": "00561048063aa2f0d85334dab039a83fe95c806cca49f1eed58a0b1c880d699d"
        },
        {
            "id": "act_network_ontology_v4",
            "title": "ACT Network Ontology",
            "version": "V4",
            "owner": "Pitt",
            "type": "Network Ontology Package",
            "networkFiles": ["https://act-ontology-v4-test.s3.amazonaws.com/network_files/AdapterMappingV4.zip"],
            "terminologies": ["CPT4", "LOINC", "ICD10CM", "UMLS"],
            "file": "https://ontology-store-v2.s3.amazonaws.com/products/act_network_ontology_v4.zip",
            "sha256Checksum": "ee11f40630bae3fb87dac755c6ddbe7cd73594ada5d4991ebd559de27351f014"
        }
    ]
}
```
<figure>
    <figcaption><b>Figure 4: </b>An example of an ontology product list</figcaption>
</figure>

## OntologyStore Cell

The OntologyStore cell install the ontology by fetching the list of ontology products from **AWS S3**, downloading the ontology package from the list of products into the **download directory** on the server, and import the ontology from the package into the **i2b2 database**.  See Figure 1.

### Data Source

Like all other cells in i2b2, the OntologyStore cell needs to communicate with your i2b2 database. The data source configuration is stored in the XML file ***ontstore-ds.xml***.

The data source, **OntologyStoreBootStrapDS**, has access to the tables in the **i2b2hive** schema.

An example datasource XML file for Oracle database:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<datasources xmlns="http://www.jboss.org/ironjacamar/schema">
    <!--
    The bootstrap points to the data source for your database lookup table which is a hivedata table, this is required.
    -->

    <!-- Oracle -->
    <datasource jta="false" jndi-name="java:/OntologyStoreBootStrapDS"
                pool-name="OntologyStoreBootStrapDS" enabled="true" use-ccm="false">
        <connection-url>jdbc:oracle:thin:@localhost:1521:xe</connection-url>
        <driver-class>oracle.jdbc.OracleDriver</driver-class>
        <driver>ojdbc11.jar</driver>
        <security>
            <user-name>i2b2hive</user-name>
            <password>demouser</password>
        </security>
        <validation>
            <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker"/>
            <validate-on-match>false</validate-on-match>
            <background-validation>true</background-validation>
            <background-validation-millis>60000</background-validation-millis>
            <use-fast-fail>true</use-fast-fail>
            <check-valid-connection-sql>SELECT 1 FROM DUAL</check-valid-connection-sql>
        </validation>
        <statement>
            <share-prepared-statements>false</share-prepared-statements>
        </statement>
    </datasource>
</datasources>
```

In addition to access the **i2b2hive** schema, the cell also access the **i2b2demodata** schema and the **i2b2metadata** schema to import the data for the concept dimension and the metadata.  The cell obtains the data source to these schemas by looking up the data source name in the ***CRC_DB_LOOKUP*** table and the ***ONT_DB_LOOKUP*** table in the **i2b2hive** schema.


### Product List URL

The URL to the ontology product list is stored in the i2b2 ***HIVE_CELL_PARAMS*** table in the i2b2 database.

Below is a table of a column values to store the URL in the i2b2 ***HIVE_CELL_PARAMS*** table:

| Column        | Value                                                        |
|---------------|--------------------------------------------------------------|
| datatype_cd   | T                                                            |
| cell_id       | ONTSTORE                                                     |
| param_name_cd | ontstore.product.list.url                                    |
| value         | https://ontology-store-v2.s3.amazonaws.com/product-list.json |
| status_cd     | A                                                            |

### Download Path

The cell needs to know the location on the server to download the ontology package (zip file) to.  The download path on the server is stored in the table ***PM_CELL_PARAMS*** in the **i2b2pm** schema.


### Ontology Download Process

Steps the OntologyStore cell goes through to download an ontology package:

1. Fetch the list of ontology products and extract the URL that points to the ontology package.
2. Download the ontology package onto the server.
3. Verify the downloaded package by omputing a SHA-256 checksum of the file and compare it against the SHA-256 checksum value from ***sha256Checksum*** attribute in the ontology product.  See Figure 3.

### Ontology Install Process

Steps the OntologyStore cell goes through to install an ontology package:

1. Open the ontology package (zip file).
2. Read the package.json inside the zip file to locate the files for the metadata, concept dimension, etc
3. Create a new metadata table to import the data from file.  The table name is based on the metadata file name.
4. Create a new concept dimension to import the data from file.  The table is based on the concept-dimension file name.
5. Import all other data directly to the i2b2 tables.

> Note: To save space on the server, the cell does not unzip ontology package.  It reads the file directly from file reader stream.

### Disable Installed Ontologies

Ontologies that have been installed can be hide from the users by disabling them.  The cell hide the ontologies by modifying the value of the **C_VISUALATTRIBUTES** column in the i2b2 ***TABLE_ACCESS*** table.

See the i2b2 documentation on [C_VISUALATTRIBUTES](https://community.i2b2.org/wiki/display/ServerSideDesign/C_VISUALATTRIBUTES) for more detail.

## OntologyStore Plugin

The OntologyStore plugin is an i2b2 webclient plugin that lists ontologies hosted on the cloud, enable i2b2 administrators to download ontologies, install ontologies, and disable (hide) installed ontologies from users.

The plugin communicates with the i2b2 Hive via pre-defined XML messages over RESTful services.  The request goes to the PM cell first for authetication, ensure that the user is authenticated and has admin role, and then goes to the OntologyStore cell.  See the i2b2 [Web Client Architecture Guide](https://community.i2b2.org/wiki/display/webclient/Web+Client+Architecture+Guide) for more detail.

### REST Endpoints

The OntologyStore plugin sends XML messages to the following REST endpoints to communicate with the OntologyStore cell:

- **getProducts**: Get the list of ontology products and their current status.
- **getProductActions**: Request to download, install and disable/enable ontologies.

### REST Endpoint Request and Response Examples

Example of ***getProducts*** request:

```xml
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
            <application_name>i2b2 OntologyStore</application_name>
            <application_version>1.8.2</application_version>
        </sending_application>
        ...
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:getProducts/>
    </message_body>
</ns3:request>
```

Example of ***getProducts*** response:
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
        ...
    </message_header>
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:products>
            <product>
                <id>act_network_ontology_v4</id>
                <title>ACT Network Ontology</title>
                <version>V4.0</version>
                <owner>Pitt</owner>
                <type>Network Ontology Package</type>
                <terminologies>
                    <terminology>CPT4</terminology>
                    <terminology>LOINC</terminology>
                    <terminology>ICD10CM</terminology>
                    <terminology>UMLS</terminology>
                </terminologies>
                <include_network_package>true</include_network_package>
                <downloaded>false</downloaded>
                <installed>false</installed>
                <started>false</started>
                <failed>false</failed>
                <disabled>false</disabled>
            </product>
            <product>
                <id>act_vax_v42</id>
                <title>ACT Vaccines</title>
                <version>V4.2</version>
                <owner>Pitt</owner>
                <type>Ontology Package</type>
                <terminologies>
                    <terminology>CPT4</terminology>
                    <terminology>CVX</terminology>
                    <terminology>CVXGROUP</terminology>
                    <terminology>NDC</terminology>
                </terminologies>
                <include_network_package>false</include_network_package>
                <downloaded>false</downloaded>
                <installed>false</installed>
                <started>false</started>
                <failed>false</failed>
                <disabled>false</disabled>
            </product>
            <product>
                <id>act_zipcode_v41</id>
                <title>ACT Zip Code</title>
                <version>V4.1</version>
                <owner>Pitt</owner>
                <type>Ontology Package</type>
                <terminologies>
                    <terminology>DEM|HRR</terminology>
                    <terminology>DEM|HSA</terminology>
                    <terminology>DEM|RUCC</terminology>
                    <terminology>DEM|SameAsSite</terminology>
                    <terminology>DEM|ZIP3</terminology>
                    <terminology>DEM|ZIPCODE</terminology>
                </terminologies>
                <include_network_package>false</include_network_package>
                <downloaded>false</downloaded>
                <installed>false</installed>
                <started>false</started>
                <failed>false</failed>
                <disabled>false</disabled>
            </product>
            ...
        </ns4:products>
    </message_body>
</ns2:response>
```

Example of ***getProductActions*** request for downloading, installing, and disabling:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions</redirect_url>
        </proxy>
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>i2b2 OntologyStore</application_name>
            <application_version>1.8.2</application_version>
        </sending_application>
        ...
    </message_header>
    <request_header>
        <result_waittime_ms>180000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <id>act_demo_v4</id>
                <title>ACT Demo</title>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>false</install>
                <disable_enable>false</disable_enable>
            </product_action>
            <product_action>
                <id>act_covid_v4</id>
                <title>ACT COVID-19 Ontology</title>
                <include_network_package>false</include_network_package>
                <download>false</download>
                <install>true</install>
                <disable_enable>false</disable_enable>
            </product_action>
            <product_action>
                <id>act_covid_v41</id>
                <title>ACT COVID-19</title>
                <include_network_package>false</include_network_package>
                <download>false</download>
                <install>false</install>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns3:request>
```

Example of ***getProductActions*** response for downloading, installing, and disabling:

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
        ...
    </message_header>
    <response_header>
        <result_status>
            <status type="DONE">OntologyStore processing completed</status>
        </result_status>
    </response_header>
    <message_body>
        <ns4:product_actions>
            <product_action>
                <id>act_demo_v4</id>
                <title>ACT Demo</title>
                <include_network_package>false</include_network_package>
                <download>true</download>
                <install>false</install>
                <disable_enable>false</disable_enable>
            </product_action>
            <product_action>
                <id>act_covid_v4</id>
                <title>ACT COVID-19 Ontology</title>
                <include_network_package>false</include_network_package>
                <download>false</download>
                <install>true</install>
                <disable_enable>false</disable_enable>
            </product_action>
            <product_action>
                <id>act_covid_v41</id>
                <title>ACT COVID-19</title>
                <include_network_package>false</include_network_package>
                <download>false</download>
                <install>false</install>
                <disable_enable>true</disable_enable>
            </product_action>
        </ns4:product_actions>
    </message_body>
</ns2:response>
```
