# OntologyStore

OntologyStore is an administrative tool that enables i2b2 administrator to download ontologies hosted on the cloud and to import them into the i2b2 database.

OntologyStore consists of the following components:

- The OntologyStore cell - assists in fetching the list of ontologies from the cloud, download the
ontologies onto a storage location and installing the downloaded ontologies.

- The OntologyStore webclient plugin - assists in displaying the list of ontologies, selecting
ontologies to download and install, and hiding (disabled) installed ontologies.

<figure>
    <img src="../img/ontstore-plugin_2.svg" alt="OntologyStore Flow Diagram" />
    <figcaption><b>Figure 1:</b> OntologyStore Flow Diagram</figcaption>
</figure>

## OntologyStore Cell

The OntologyStore cell imports the ontology by getting the list of ontologies from the cloud (AWS S3), downloading the ontology package from the list onto the server, and import the ontology into the i2b2 database.  See Figure 1.

The cell retrieves a list of ontologies by fetching the JSON data and parsing the information from it. The URL that points to the JSON data is stored in the **hive_cell_params** table of the i2b2 database.

To download the ontology, the cell fetch the list of ontology products and get the URL for the ontology package from the ***file*** attribute.  The cell download the package onto server in the location specified in the **pm_cell_params** table of the i2b2 database. The cell ensures that the file downloaded is not corrupted by computing a SHA-256 checksum of the file and compare it against the SHA-256 checksum value from ***sha256Checksum*** attribute.

### JSON Data

The JSON data is a JSON object that has an array of ontology products.

The ontology product contains the following ontology information:

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

Example of JSON data that contains a list of ontologies:
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
