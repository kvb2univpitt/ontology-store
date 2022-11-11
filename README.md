# ontology-store

An i2b2 software for downloading and installing ontologies.  The software consists of the following software components:

- [i2b2-ontologystore](cell/README.md), a backend API (i2b2 cell).
- [OntologyStore](plugin/README.md), a frontend client (i2b2 plug-in).


## Installing the Software

- Instructions for installing the [i2b2-ontologystore](cell/README.md).
- Instructions for installing the  [OntologyStore](plugin/README.md).

## Using the Software

The plug-in requires users to have i2b2 **administrative privileges**.

1. Log on to the i2b2 web client as an administrator.

2. Click on the ***Analysis Tools*** drop-down.
![Select Analysis Tools](img/select_analysis_tool.png)

3. Click on **Ontology Store**.
![Select Ontology Store](img/select_ontologystore.png)

4. Select the ontology to download and install by checking the checkboxes and click the Execute button.  For example, we will download and install the ***ACT Vital Signs Ontology*** and just download the ***ACT Visit Details Ontology***.
![Select Ontologies](img/select_ontologies.png)

5. A spinner will show while the tasks are in progress.
![In Progress](img/in_progress.png)

6. Once the task is done, a summary of the task will pop up.
![Summary Status](img/summary_message.png)

7. You will see that the ***ACT Vital Signs Ontology*** has been installed and the ***ACT Visit Details Ontology*** has been downloaded.
![Ontology Downloaded and Installed](img/ontology_installed.png)
