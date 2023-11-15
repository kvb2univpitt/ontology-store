# OntologyStore Quick Installation Guide

A simple guide for installing the OntologyStore software.

## Prerequisites

### i2b2 System Requirements

- i2b2 Core Server 1.7.13 Release.
- i2b2 Webclient 1.7.13 Release.

### Required Permissions

- System administrator privileges is needed to install the OntologyStore plugin and to install the OntologyStore cell.
- i2b2 administrator privileges is needed to log into the i2b2 Administration Module to add new cell.

## Installing the OntologyStore Cell

Assume the Wildfly directory is **/opt/wildfly**.

### 1. Stop the Current Running Services

- Stop the Wildfly server running the i2b2 core server.

### 2. Download the OntologyStore Cell

- Click on the link to download [ontologystore_cell.zip](https://drive.google.com/file/d/1Pjkmc1AO2WWyg2jhUMrqn3PUZ8YmNHWS/view?usp=sharing).
- Extract ***ontologystore_cell.zip*** file.  Once the file has been unzip, there should be two files (***OntologyStore.aar*** and ***OntologyStore.jar***) in the folder **ontologystore_cell**.

### 3. Add the Files to the i2b2 Cell

- Copy the aar file ***OntologyStore.aar*** into the Wildfly directory **/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/services**.

- Copy the jar file ***OntologyStore.jar*** into the Wildfly directory **/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/lib**.

### 4. Configure the i2b2 OntologyStore cell

- Create a file called ***ontologystore.properties*** in the Wildfly configuration directory **/opt/wildfly/standalone/configuration** with the following content:

```properties
ontology.dir.download=ontology_download_storage_directory

aws.s3.json.product.list=https://ontology-store.s3.amazonaws.com/product-list.json

# datasources
spring.hive.datasource.jndi-name=java:/OntologyBootStrapDS
spring.pm.datasource.jndi-name=java:/PMBootStrapDS
```

- Replace the ***ontology_download_storage_directory*** with the path to the directory where the ontologies should be downloaded to.

### 5. Restart the Services

- Restart the Wildfly server running the i2b2 core server.

## Installing the OntologyStore Plugin

Assume that the i2b2 webclient directory is **/var/www/html/webclient**.

### 1. Stop the Current Running Services

- Stop the web server running the i2b2 webclient.

### 2. Download the OntologyStore Cell

- Click on the link to download [ontologystore_plugin.zip](https://drive.google.com/file/d/1YqbbO-nFtcdfRXQWbaFAfdSBkXbQzGrY/view?usp=sharing).
- Extract ***ontologystore_plugin.zip*** file.  Once the file has been unzip, there should be a folder called **OntologyStore**.

### 3. Add the Plugin to the i2b2 Webclient

- Copy the folder **OntologyStore**, extracted from the ***ontologystore_plugin.zip*** file, into the i2b2 webclient plugin directory **/var/www/html/webclient/js-i2b2/cells/plugins/standard**.

### 4. Configure the i2b2 Webclient

- Add the following code to the array i2b2.hive.tempCellsList in the module loader configuration file ***i2b2_loader.js*** located in the directory **/var/www/html/webclient/js-i2b2**.

```js
{code: "ONTSTORE"},
{code: "OntologyStore",
    forceLoading: true,
    forceConfigMsg: {params: []},
    roles: [ "MANAGER" ],
    forceDir: "cells/plugins/standard"
}
```

### 5. Restart the Services

- Restart the web server running the i2b2 webcleint.