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

    ![Login Page](img/ont-plugin.png)
    