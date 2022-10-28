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

| Username | Password | Role  | Domain   |
|----------|----------|-------|----------|
| demo     | demouser | user  | i2b2demo |
| i2b2     | i2b2user | admin | i2b2demo |

### Getting a List of Ontologies

To get a list of ontologies, make the following SOAP call:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        <proxy>
            <redirect_url>http://localhost:9090/i2b2/services/OntologyStoreService/getProducts</redirect_url>
        </proxy>
        <sending_application>
            <application_name>i2b2 Ontology Service</application_name>
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