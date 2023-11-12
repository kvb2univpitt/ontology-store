# OntologyStore Quick Installation

A guide for quick installation of OntologyStore cell and plugin.

## Prerequisites

The following are required:

- i2b2 (Core Server and Web Client) 1.7.13 Release.
- i2b2 administrator privileges.
- Server administrator privileges.

## Installing the OntologyStore Cell

### 1. Stop the Current Running Services

Stop the Wildfly server and the web server.

### 2. Download the OntologyStore Cell

- Click on the link to download [ontologystore_cell.zip](https://drive.google.com/file/d/1Pjkmc1AO2WWyg2jhUMrqn3PUZ8YmNHWS/view?usp=sharing).
- Extract ***ontologystore_cell.zip*** file.  Once the file has been unzip, there should be two files (***OntologyStore.aar*** and ***OntologyStore.jar***) in the folder **ontologystore_cell**.

### 3. Add the Files to the i2b2 Cell

Assuming the Wildfly director is **/opt/wildfly**.

- Copy the aar file ***OntologyStore.aar*** into the Wildfly directory **/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/services**.

- Copy the jar file ***OntologyStore.jar*** into the Wildfly directory **/opt/wildfly/standalone/deployments/i2b2.war/WEB-INF/lib**.
