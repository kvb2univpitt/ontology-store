# ontology-store

An i2b2 administrative tool for downloading and installing ontologies hosted on a cloud.

<figure>
    <img src = "./img/ontology-plugin.svg" alt="OntologyStore Design" width="100%" height="100%" />
    <figcaption align="center">
        <b>Fig. 1 - Ontology Store Flow</b>
    </figcaption>
</figure>

The software consists of the following software components:

- [i2b2-ontologystore](cell), a backend API (i2b2 cell).
- [OntologyStore](plugin), a frontend client (i2b2 plug-in).

## Installing the Software

- [Quick installation guide](docs/QUICK_INSTALL_i2b2_1.8.2.md).


## Using the Software

The plug-in requires users to have i2b2 **administrative privileges**.

1. Log on to the i2b2 web client as an administrator.

2. Click on the ***Analysis Tools*** link.
![Select Analysis Tools](img/select_analysis_tool.png)

3. Scroll down the list until you see the **Ontology-Store Plugin**.  Click on it.
![Select Ontology Store](img/select_ontologystore.png)

4. Select the ontology to download and install by checking the checkboxes and click the Execute button.  For example, we will download and install the ***ACT Vital Signs Ontology*** and just download the ***ACT Visit Details Ontology***.
![Select Ontologies](img/select_ontologies.png)

5. A spinner will show while the tasks are in progress.
![In Progress](img/in_progress.png)

6. Once the task is done, a summary table of the tasks will pop up.
![Summary Status](img/summary_message.png)

7. You will see that the ***ACT Vital Signs Ontology*** has been successfully installed and the ***ACT Visit Details Ontology*** has been successfully downloaded.
![Ontology Downloaded and Installed](img/ontology_installed.png)