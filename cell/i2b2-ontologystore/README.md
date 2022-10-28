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
