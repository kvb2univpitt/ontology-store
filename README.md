# ontology-store

An i2b2 plug-in providing the following functionalities:

- Downloading i2b2 formatted ontologies, hosted on the Amazon S3 cloud, onto the server.
- Importing the downloaded ontologies and data into an i2b2 database.

## ontology-store-ws

The **ontology-store-ws** is the Ontology-Store REST API for retrieving a list ontologies host on Amazon S3 cloud, for downloading the ontologies, and for importing the ontologies into an i2b2 database.

### Building the REST API

#### Prerequisite
- [Java 8 and above](https://developers.redhat.com/products/openjdk/download)
- [Apache Maven 3.x.x](https://maven.apache.org/download.cgi)


#### Setting the Ontology Download Location

The location where the ontologies will be downloaded to on the server is specified in the **application.properties** file located in the project folder ***ontology-store-ws/src/main/***.  Set the ***ontology.dir.download*** property to the path of the location.  For an example, if the download location on your server is ***/home/wildfly/download***, the **application.properties** should be to the following:

```properties
ontology.dir.download=/home/wildfly/download

aws.s3.json.product.list=https://ontology-store.s3.amazonaws.com/product-list.json

# CRC datasource
spring.datasource.jndi-name=java:/OntologyBootStrapDS
```

#### Building the REST API WAR File

Open a terminal in the project folder ***ontology-store-ws***.  Execute the following command to run Apache Maven to build the war file:

```
mvn clean package
```

The war file, **ontology-store.war**, is located in the project folder ***ontology-store-ws/target***.

### Deploying the WAR File

The Ontology-Store REST API must be running in the same Servlet container as the i2b2 Hive.  The REST API shares the JNDI database configuration as the i2b2 Hive.

1. Stop Wildfly.
2. Copy the **ontology-store.war** file to Wildfly's **deployments** folder.
3. Start Wildfly.

> Note that the instructions above require administrative privileges.

### Proxying OntologyStore Request

The request to ontologystore API is made to the path ***/ontology-store*** on the same server (hostname) where the i2b2 webclient is hosted.  The request is then gets proxied over to the Wildfly server.

Assuming your Wildfly server is running on host ***localhost*** on port ***9090***, create a file called ***ontologystore.conf*** in the directory **/etc/httpd/conf.d/** with the following content:

```conf
ProxyPass /ontology-store http://localhost:9090/ontology-store
ProxyPassReverse /ontology-store http://localhost:9090/ontology-store
ProxyTimeout 3000
```

> Remember to restart your Apache web server.

For more information on proxying, please visit [Apache Module mod_proxy](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html).

## OntologyStore

An i2b2 plug-in that allow users to download ontologies and import them into an i2b2 database by making API calls to the Ontology-Store REST API.

### Installing the Plug-in

#### Copy the Plug-in to the i2b2 Web Client

Copy the folder **edu** located in the project folder **ontology-store/plugin** to the directory ***plugins*** of the i2b2 web client.

#### Registering the Plug-in

Registering the plug-in with the web client framework by adding the entry ```"edu.pitt.dbmi.ontology"``` to the file **plugins.json** located in the folder ***plugins*** of the web client.

For an example, the **plugins.json** file should look similar to this:

```json
[
    "edu.harvard.catalyst.example",
    ...
    "edu.pitt.dbmi.ontology"
]
```

## Using the Plug-in

The plug-in requires users to have i2b2 administrative privileges.

1. Log on to the i2b2 web client as an **administrator**.
2. Click on the ***Analysis Tools*** link.

    ![Click Analysis Tool](img/click_analysis_tool.png)
3. Click on the **Ontology Store** plugin.

    ![Click OntologyStore Plugin](img/click_ontologystore.png)
4. Click on the icon ***Sync From Cloud*** to retrieve a list of ontologies from AWS to download/install.

    ![Click Sync From Cloud](img/click_sync_from_cloud.png)
5. Select the ontology to download and install by checking the checkboxes and click the ***Execute*** button.

    ![Click the Execute Button](img/click_execute_button.png)
6. A spinner will show while tasks are in progress.

    ![Task in Progress](img/execute_progress.png)
7. Once the task is done, a summary of the task will pop up.

    ![Summary](img/summary.png)
8. Log out and log back in.  You will see the installed ontologies.

    ![Ontology Installed](img/ontology_installed.png)
