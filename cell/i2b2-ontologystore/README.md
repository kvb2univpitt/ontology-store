# i2b2-ontologystore

An i2b2 cell for downloading ontologies onto the server and for installing ontologies into i2b2 database.

## Building the i2b2 Cell

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
