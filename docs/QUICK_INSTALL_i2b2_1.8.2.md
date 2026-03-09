# OntologyStore Installation Guide

This guide will help you install the OntologyStore software for **i2b2 1.8.2 Release**.

The OntologyStore software consists of the following components:

* The i2b2 OntologyStore plugin.
* The i2b2 OnologyStore cell.

The OntologyStore plugin is an i2b2 webclient plugin that allows users to see the list of available ontologies for downloading and installing into the i2b2 database.

The OntologyStore cell is an i2b2 cell that fetches a list of available ontologies from the cloud, downloads ontologies onto the server, and installs downloaded ontologies.

## Installing the i2b2 OntologyStore Cell

The following instructions assume that the Wildfly is installed on the server and is located at ```/opt/wildfly```.

### Prerequisites

- i2b2 Core Server *1.8.2 Release*.
- System administrator privileges for adding AAR file, JAR file, and datasource file (*-ds.xml) to Wildfly.

#### 1. Stop Wildfly Server

- Stop the current Wildfly server where the i2b2 cells are deployed:
    ```
    sudo systemctl stop wildfly.service
    ```

#### 2. Download the Cell

- Click on [ontstore_cell.zip](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ontstore_cell.zip) to download the file.

- Extract ***ontstore_cell.zip*** file.  Once extracted, you should see the followin files:

    - WEB-INF/services/OntologyStore.aar
    - WEB-INF/lib/OntologyStore.jar

#### 3. Add the OntologyStore Cell to the i2b2 WAR File

Update the i2b2 WAR file by adding the files to the OntologyStore cell files.

- Add the **OntologyStore.aar** file from the ***WEB-INF/services*** folder to the ***WEB-INF/services*** folder in the i2b2 WAR file.

- Add the **OntologyStore.jar** file from the ***WEB-INF/lib*** folder to the ***WEB-INF/lib*** folder in the i2b2 WAR file.

##### Linux Command

Below is the Linux command for updating the i2b2 WAR file using the ***jar*** command from the Java SDK:

```bash
jar -uvf i2b2.war WEB-INF/services/OntologyStore.aar
jar -uvf i2b2.war WEB-INF/lib/OntologyStore.jar
```

#### 4. Add the Datasource Configuration File

- Download the datasource file **ontstore-ds.xml** based on your database vendor:

    | Database Vendor | Datasource File                                                                                                    |
    |-----------------|--------------------------------------------------------------------------------------------------------------------|
    | Oracle          | [ontstore-ds.xml](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ds/oracle/ontstore-ds.xml)     |
    | SQL Server      | [ontstore-ds.xml](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ds/sqlserver/ontstore-ds.xml)  |
    | PostgreSQL      | [ontstore-ds.xml](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ds/postgresql/ontstore-ds.xml) |

- Replace the value for the ***connection-url***, ***user-name***, and ***password*** in the file **ontstore-ds.xml** with your database *connection url*, *username*, and *password*, respectively.
- Place the file **ontstore-ds.xml** in the directory ```/opt/wildfly/standalone/deployments```.

> See the i2b2 documentation on [Data Source Configuration](https://community.i2b2.org/wiki/display/getstarted/2.+Data+Source+Configuration) for more detail.

#### 5. Restart Wildfly Server

- Change the file ownership of all the files added to Wildfly:
    ```
    sudo chown -RH wildfly: /opt/wildfly/
    ```
- Restart Wildfly server:
    ```
    sudo systemctl restart wildfly.service
    ```

- You can verify that the OntologyStore service is available from the list of services: [http://localhost:9090/i2b2/services/listServices](http://localhost:9090/i2b2/services/listServices).

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

    | Column        | Value                                                                          |
    |---------------|--------------------------------------------------------------------------------|
    | datatype_cd   | T                                                                              |
    | cell_id       | ONTSTORE                                                                       |
    | param_name_cd | ontstore.product.list.url                                                      |
    | value         | https://ontology-store-v2.s3.us-east-1.amazonaws.com/product-list-aws-all.json |
    | status_cd     | A                                                                              |

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

Note that the directory path **/home/wildfly** and **/home/wildfly/ontology** must be owned by ***wildfly***. Execute the following to change the ownership to ***wildfly***:

```
sudo chown -RH wildfly: /home/wildfly
```

## Installing the Plugin

The following instructions assume that the i2b2 webclient is installed in the directory ```/var/www/html/webclient```.

### Prerequisites

- i2b2 Web Client *1.8.x Release*.
- System administrator privileges for updating the Web Client.

#### 1. Stop the Apache HTTP Server

- Stop the web server running the i2b2 webclient.

#### 2. Add the OntologyStore Plugin Communicator

The plugin communicator is the communication channel between the OntologyStore plugin and the OntologyStore cell.

-  Click on [ONTSTORE.zip](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ONTSTORE.zip) to download the file.

- Extract the ***ONTSTORE.zip*** file to the following i2b2 webclient directory:

    ```
    /var/www/html/webclient/js-i2b2/cells
    ```

- Add the following content to the file ***i2b2_config_cells.json*** located at **/var/www/html/webclient**
    ```
    { "code": "ONTSTORE" }
    ```

#### 3. Add the Plugin to the i2b2 Webclient

- Click on [ontology-store.zip](https://pitt-dbmi.s3.us-east-1.amazonaws.com/ontology-store/1.8.2/ontology-store.zip) to download the file.

- Extract file ***ontology-store.zip*** to the following i2b2 webclient plugin directory:

    ```
    /var/www/html/webclient/plugins/edu/pitt/dbmi
    ```

    Note that the path **edu/pitt/dbmi** may not exist in the directory path **/var/www/html/webclient/plugins**.  You can create it by execute the command ```mkdir -p edu/pitt/dbmi``` in the directory **/var/www/html/webclient/plugins**.

- Add the following content to the file ***plugins.json*** located in the directory ```/var/www/html/webclient/plugins```:

    ```
    "edu.pitt.dbmi.ontology-store"
    ```

#### 4. Restart the Apache HTTP Server

- Restart the web server running the i2b2 webcleint:
    ```
    sudo systemctl restart httpd.service
    ```
