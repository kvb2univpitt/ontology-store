# Quick Installation Guide for the OntologyStore v0.5.0

A guide for installing the OntologyStore software.

The OntologyStore has two components:

1. The i2b2 OntologyStore cell (back-end).
2. The i2b2 OntologyStore plugin (front-end).

## Installing the i2b2 OntologyStore Cell

The following instructions assume that the Wildfly is installed on the server and is located at ```/opt/wildfly```.

### Prerequisites

- i2b2 Core Server *1.7.13 Release* or *1.8.0 Release*.
- System administrator privileges for adding AAR file, JAR file, and datasource file (*-ds.xml) to Wildfly.

#### 1. Stop Wildfly Server

- Stop the current Wildfly server where the i2b2 cells are deployed.

#### 2. Download the Cell

- Click on [ontologystore_cell.zip](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/ontologystore_cell.zip) to download the file.

- Extract ***ontologystore_cell.zip*** file.  Once extracted, there should be a folder called **ontologystore_cell** containing the followin files:
    - OntologyStore.aar
    - OntologyStore.jar

#### 3. Deploy the OntologyStore Cell in Wildfly

- Copy the **OntologyStore.aar** file from the ***ontologystore_cell*** folder to the i2b2 ***WEB-INF/services*** directory ```/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/services```.

- Copy the **OntologyStore.jar** file from the ***ontologystore_cell*** folder to the i2b2 ***WEB-INF/lib*** directory ```/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/lib```.

> Note that the ***i2b2.war*** in the Wildfly directory ```/opt/wildfly/standalone/deployments``` may be an actual WAR file instead of a directory.  In this case, you will need to open up the ***i2b2.war*** file and add the ***OntologyStore.aar*** file to the ```WEB-INF/services``` folder and the ***OntologyStore.jar*** file to the ```WEB-INF/lib``` folder.

#### 4. Add the Datasource Configuration File

- Download the datasource file **ontstore-ds.xml** based on your database vendor:

    | Database Vendor | Datasource File                                                                                          |
    |-----------------|----------------------------------------------------------------------------------------------------------|
    | Oracle          | [ontstore-ds.xml](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/ds/oracle/ontstore-ds.xml)     |
    | SQL Server      | [ontstore-ds.xml](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/ds/sqlserver/ontstore-ds.xml)  |
    | PostgreSQL      | [ontstore-ds.xml](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/ds/postgresql/ontstore-ds.xml) |

- Replace the value for the ***connection-url***, ***user-name***, and ***password*** in the file **ontstore-ds.xml** with your database *connection url*, *username*, and *password*, respectively.
- Place the file **ontstore-ds.xml** in the directory ```/opt/wildfly/standalone/deployments```.

> See the i2b2 documentation on [Data Source Configuration](https://community.i2b2.org/wiki/display/getstarted/2.+Data+Source+Configuration) for more detail.

#### 5. Restart Wildfly Server

## Configuring the OntologyStore Cell

### Prerequisites

- i2b2 database administrator privileges for adding entries into the i2b2 database tables.

#### 1. Add the Cell

All of the back-end communications go through the PM Cell first then get pass to the designated cell.  For the PM cell to recognize the OntologyStore cell, the cell information must be added to the **pm_cell_data** table in the i2b2 database.

Assume that the i2b2 Core Servers is deployed in Wildfly with the hostname ***localhost*** on port ***9090***

- Insert the following data to the i2b2 database table **pm_cell_data** to add the OntologyStore cell.

    | Column       | Value                                                     |
    |--------------|-----------------------------------------------------------|
    | Cell ID      | ONTSTORE                                                  |
    | Cell Name    | OntologyStore Cell                                        |
    | Cell URL     | http://localhost:9090/i2b2/services/OntologyStoreService/ |
    | Project Path | /                                                         |
    | Method       | REST                                                      |

    > See the i2b2 documentation on [Add a New Cell](https://community.i2b2.org/wiki/display/getstarted/6.4.1.1+Add+a+New+Cell) for more detail.

#### 2. Configure Cell Properties

The OntologyStore has two properties that need to be set:

1. The location (URL) of the file (JSON) containing a list of ontologies to download.
2. The location on the server to download the ontologies to.

##### Set the URL Location
- Insert the following data to the **hive_cell_params** table to set the cell properties:

    | Column        | Value                                                        |
    |---------------|--------------------------------------------------------------|
    | datatype_cd   | T                                                            |
    | cell_id       | ONTSTORE                                                     |
    | param_name_cd | ontstore.product.list.url                                    |
    | value         | https://ontology-store-v2.s3.amazonaws.com/product-list.json |
    | status_cd     | A                                                            |

    > See the i2b2 documentation on [Configure cell properties](https://community.i2b2.org/wiki/pages/viewpage.action?pageId=28639260) for more detail.

##### Set the Download Location

The configuration below tells the OntologyStore cell where to download the ontologies.  Assume that the directory where the ontologies are downloaded to is ```/home/wildfly/ontology```

- Insert the following data to the **pm_cell_params** table to set the additional cell properties:

    | Column        | Value                  |
    |---------------|------------------------|
    | datatype_cd   | T                      |
    | cell_id       | ONTSTORE               |
    | project_path  | /                      |
    | param_name_cd | ontstore.dir.download  |
    | value         | /home/wildfly/ontology |
    | status_cd     | A                      |

    > See the i2b2 documentation on [Add Cell Parameters](https://community.i2b2.org/wiki/display/getstarted/6.4.2.1+Add+Cell+Parameters) for more detail.

## Installing the Plugin

The following instructions assume that the i2b2 webclient directory is ```/var/www/html/webclient```.

### Prerequisites

- i2b2 Web Client *1.7.13 Release* or *1.8.0 Release*.
- System administrator privileges for updating the Web Client.

#### 1. Stop the Apache HTTP Server

- Stop the web server running the i2b2 webclient.

#### 2. Add the OntologyStore Plugin Communicator

The plugin communicator is the communication channel between the OntologyStore plugin and the OntologyStore cell.

-  Click on [ONTSTORE.zip](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/ONTSTORE.zip) to download the file.

- Extract the ***ONTSTORE.zip*** file to the following i2b2 webclient directory:

    | Web Client Version | Directory                                                                      |
    |--------------------|--------------------------------------------------------------------------------|
    | 1.7.13 Release     | /var/www/html/webclient/js-i2b2/cells                                          |
    | 1.8.x Release      | /var/www/html/webclient/js-i2b2/cells/LEGACYPLUGIN/legacy_plugin/js-i2b2/cells |

#### 3. Add the Plugin to the i2b2 Webclient

- Click on [OntologyStore.zip](https://pitt-dbmi.s3.amazonaws.com/ontology-store/0.5.0/OntologyStore.zip) to download the file.

- Extract file ***OntologyStore.zip*** to the following i2b2 webclient plugin directory:

    | Web Client Version | Directory                                                                                        |
    |--------------------|--------------------------------------------------------------------------------------------------|
    | 1.7.13 Release     | /var/www/html/webclient/js-i2b2/cells/plugins/community                                          |
    | 1.8.x Release      | /var/www/html/webclient/js-i2b2/cells/LEGACYPLUGIN/legacy_plugin/js-i2b2/cells/plugins/community |

#### 4. Configure the i2b2 Webclient

- Add the following code to the Javascript array *i2b2.hive.tempCellsList* in the module loader configuration file **i2b2_loader.js**:

    ```js
    {code: "ONTSTORE"},
    {code: "OntologyStore",
        forceLoading: true,
        forceConfigMsg: {params: []},
        roles: [ "MANAGER" ],
        forceDir: "cells/plugins/community"
    }
    ```

    The **i2b2_loader.js** file is located in the following directory:

    | Web Client Version | Directory                                                                |
    |--------------------|--------------------------------------------------------------------------|
    | 1.7.13 Release     | /var/www/html/webclient/js-i2b2                                          |
    | 1.8.x Release      | /var/www/html/webclient/js-i2b2/cells/LEGACYPLUGIN/legacy_plugin/js-i2b2 |

    > Remember to make a backup copy of the file before modifying it.

#### 5. Restart the Apache HTTP Server

- Restart the web server running the i2b2 webcleint.
