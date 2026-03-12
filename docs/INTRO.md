# OntologyStore

OntologyStore is an administrative tool that enables i2b2 administrator to download ontologies hosted on the cloud and to import them into the i2b2 database.

OntologyStore consists of the following components:

- The OntologyStore cell - assists in fetching the list of ontologies from the cloud, download the
ontologies onto a storage location and installing the downloaded ontologies.

- The OntologyStore webclient plugin - assists in displaying the list of ontologies, selecting
ontologies to download and install, and hiding (disabled) installed ontologies.

<figure>
    <img src="../img/ontstore-plugin_2.svg" alt="OntologyStore Flow Diagram" />
    <figcaption><b>Figure 1: </b>OntologyStore Flow Diagram</figcaption>
</figure>

## Ontology Files

The ontology consists of the following tab-delimited (tsv) files:

<dl>
    <dt><strong>1. Metadata Files</strong></dt>
    <dd>
        A new metadata table will be created for importing the data.  The filename will be used as the metadata table name.
    </dd>
    <dt><strong>2. Concept Dimension Files</strong></dt>
    <dd>
        A new concept dimension table will be created for importing the data.  The filename suffixed with <b><i>_cd</i></b> will be used as the concept dimension table name.  To run queries, the data must be copied over to the main i2b2 <b>Concept Dimension</b> table.
    </dd>
    <dt><strong>3. Schemes File</strong></dt>
    <dd>Data will be directly imported into the i2b2 <b>Scheme</b> table</dd>
    <dt><strong>4. Table Access File</strong></dt>
    <dd>Data will be directly imported into the i2b2 <b>Table Access</b> table</dd>
</dl>

## Ontology Package

The ontology package is a zip file containing the ontology files along with a JSON file called ***package.json***.

The package.json tell the OntologyStore cell which files are the metadata files, which files are the concept  dimension files, and etc.

```json
{
    "tableAccess": [
        "table_access/TABLE_ACCESS.tsv"
    ],
    "schemes": ["metadata/SCHEMES.tsv"],
    "breakdownPath": ["crc/QT_BREAKDOWN_PATH.tsv"],
    "conceptDimensions": [
        "crc/ACT_COVID_V4_CD.tsv",
        "crc/ACT_CPT4_PX_V4_CD.tsv",
        "crc/ACT_DEM_V4_CD.tsv"
    ],
    "domainOntologies": [
        "metadata/ACT_COVID_V4.tsv",
        "metadata/ACT_CPT4_PX_V4.tsv",
        "metadata/ACT_DEM_V4.tsv"
    ]
}
```
<figure>
    <figcaption><b>Figure 2: </b>An example of package.json</figcaption>
</figure>

## Ontology Product

The ontology product is a JSON object containing the description of the ontology and the URL to download the ontology package.

The ontology product contains the following information (attributes):

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

```json
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
```
<figure>
    <figcaption><b>Figure 3: </b>An example of an ontology product</figcaption>
</figure>


## Ontology Product List

The Ontology product list is a JSON object containing a list of the ontology products.  This is what the OntologyStore cell fetches to get the list of onotologies.

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
<figure>
    <figcaption><b>Figure 4: </b>An example of an ontology product list</figcaption>
</figure>

## OntologyStore Cell

The OntologyStore cell install the ontology by fetching the list of ontology products from **AWS S3**, downloading the ontology package from the list of products into the **download directory** on the server, and import the ontology from the package into the **i2b2 database**.  See Figure 1.

The URL to the ontology product list is stored in the **hive_cell_params** table in the i2b2 database.

To download the ontology, the cell fetch the list of ontology products and get the URL for the ontology package from the ***file*** attribute.  The cell download the package onto server in the location specified in the **pm_cell_params** table of the i2b2 database. The cell ensures that the file downloaded is not corrupted by computing a SHA-256 checksum of the file and compare it against the SHA-256 checksum value from ***sha256Checksum*** attribute.
