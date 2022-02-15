# ontology-store

An i2b2 plug-in for downloading and installing ontology.

## Install the OntologyStore Plug-in

The instruction below is for installing OntologyStore plug-in for the i2b2 web client.  It assumes you have some understanding of the i2b2 plug-in structure.  If you are not familiar with the plug-in structure, please take a look at the i2b2 [Web Client Plug-in Developers Guide](https://community.i2b2.org/wiki/display/webclient/Web+Client+Plug-in+Developers+Guide) for tutorial on how to create custom plug-in modules that extend the functionality of the base i2b2 Web Client. 

### Prerequisites

- [i2b2-webclient](https://github.com/i2b2/i2b2-webclient)

### Install the Plug-in

Assume that the i2b2 web client location on the server is **/var/www/html/webclient**.

1. Copy the folder **OntologyStore** to the directory **/var/www/html/webclient/js-i2b2/cells/plugins/standard**.

2. Add the following code to the ```i2b2.hive.tempCellsList``` in the file ***webclient/js-i2b2/i2b2_loader.js***:
    ```js
    {code: "OntologyStore",
        forceLoading: true,
        forceConfigMsg: {params: []},
        forceDir: "cells/plugins/standard"
    }
    ```
    
    It should look similiar to this:

    ```js
    i2b2.hive.tempCellsList = [
        {code: "PM",
            forceLoading: true 			// <----- this must be set to true for the PM cell!
        },
        {code: "ONT"},
        {code: "CRC"},
        {code: "WORK"},
        ...
        {code: "OntologyStore",
            forceLoading: true,
            forceConfigMsg: {params: []},
            forceDir: "cells/plugins/standard"
        }
    ];
    ```

Log into i2b2 web client.  Click on the ***Analysis Tools*** drop-down.  You should see the OntologyStore plug-in:

![OntologyStore Plug-in](img/ont-plugin.png)

## Install the Ontology Web Services

### Prerequisites

- Java SDK 8 ([Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://adoptopenjdk.net/))
- [Apache Maven 3.x.x](https://maven.apache.org/download.cgi)
- [Wildfly 17.0.1.Final](https://download.jboss.org/wildfly/17.0.1.Final/wildfly-17.0.1.Final.zip)
- [i2b2-core-server](https://github.com/i2b2/i2b2-core-server)
- AWS S3 Account

### Configure the Ontology-Store Web Service

Set the attribute **ontology.dir.download** in the ***ontology-store-ws/src/main/resources/application.properties*** file to the location on the server where the ontology should be downloaded to.

For an example, assume the location to where the downloaded ontology files is **/home/wildfly/ontology-store/**
```properties
ontology.dir.download=/home/wildfly/ontology-store
ontology.aws.s3.bucket.name=ontology-store
ontology.aws.s3.key.name=products

spring.datasource.jndi-name=java:/OntologyDemoDS
```

### Build the Ontology-Store Web Services War File

Open up a terminal to where the file ***pom.xml*** in the folder **ontology-store-ws**.  Type the following command to execute Maven to compile and build the war file:

```
mvn clean package
```

The war file ***ontology-store.war*** will be created and is located in the directory **ontology-store-ws/target/**

### Deploy the War File to Wildfly 17.0.1

Assume the location of Wildfly is **/opt/wildfly-17.0.1.Final**.

1. Stop Wildfly service.
2. Copy the file ***ontology-store-ws/target/ontology-store.war*** to the Wildfly directory **/opt/wildfly-17.0.1.Final/standalone/deployments/**.
3. Create a file ***ontology-store.war.dodeploy*** in the directory **/opt/wildfly-17.0.1.Final/standalone/deployments/**.
4. Start Wildfly service.

### Set AWS Credentials

AWS credentials are required to access AWS S3.  The AWS credentials are stored in a local file named ***credentials***, in a folder named **.aws** in your home directory.  This is a common practice for storing AWS credentials.  See [Configuration and credential file settings](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for more detail.

The ***credentials*** file:

```text
[default]
aws_access_key_id=
aws_secret_access_key=
```

#### Example

Assume the following:

| Attribute             | Value                                    |
|-----------------------|------------------------------------------|
| User Home Directory   | /home/wildfly                            |
| AWS Access Key ID     | AKIAIOSFODNN7EXAMPLE                     |
| AWS Secret Access Key | wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY |

The ***/home/wildfly/.aws/credentials*** file should look like this:
```text
[default]
aws_access_key_id=AKIAIOSFODNN7EXAMPLE
aws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

### Configure Apache HTTP mod_proxy

1. Added the following to the file ***/etc/httpd/conf/httpd.conf***:

    ```conf
    ProxyPass /ontology-store http://localhost:9090/ontology-store
    ProxyPassReverse /ontology-store http://localhost:9090/ontology-store
    ProxyTimeout 3000
    ```

    The above configuration will take any request to the URL **http://localhost/ontology-store** and foward it to the URL **http://localhost:9090/ontology-store**.

2. Restart the Apache HTTP server.

## Download and Install Ontology

1. Log on to the i2b2 web client as an **administrator**.
2. Click on the ***Analysis Tools*** drop-down and select the **Ontology Store**.
    ![Select OntologyStore Plug-in](img/select_ont_plugin.png)
3. Click on the icon ***Sync From Cloud*** to retrieve a list of ontologies from AWS to download/install.
    ![Click Sync From Cloud Button](img/click_sync_from_cloud.png)
4. Select the ontology to download and install by checking the checkboxes and click the ***Execute*** button.
    ![Download and Install Ontology](img/click_execute_btn.png)
5. A spinner will show while tasks are in progress.
    ![Progress](img/progress.png)
6. Once the task is done, a summary of the task will pop up.
    ![Summary](img/summary.png)
7. Log out and log back in.  You will see the installed ontologies.
    ![Ontology Installed](img/ontology_installed.png)
