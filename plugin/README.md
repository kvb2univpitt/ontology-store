# OntologyStore

An i2b2 plugin providing the following functionalities:

- Download the i2b2 ontologies from the cloud onto the server.
- Import the dowloaded ontologies into an i2b2 database.

The ontologies are provided by the community and are publicly available on the AWS cloud.

## Installing the Plug-in

### Prerequisites

- [i2b2-ontologystore](../cell/README.md)

### Copy the Plug-in to the i2b2 Web Client

Copy the folder **OntologyStore** located in the project folder ```ontology-store/plugin``` to the i2b2 webclient directory ```webclient/js-i2b2/cells/plugins/standard```.

### Registering the Plug-in

To register the plug-in with the i2b2 webclient, add the following code to the array ***i2b2.hive.tempCellsList*** in the module loader configuration file **i2b2_loader.js** located in the i2b2 webclient directory ```webclient/js-i2b2```:

```js
{code: "OntologyStore",
    forceLoading: true,
    forceConfigMsg: {params: []},
    forceDir: "cells/plugins/standard"
}
```

For an example, the **i2b2_loader.js** file should look similar to this:

```js
i2b2.hive.tempCellsList = [
    {code: "PM",
        forceLoading: true 			// <----- this must be set to true for the PM cell!
    },
    {code: "ONT"},
    {code: "CRC"},
    {code: "WORK"},
    {code: "ONTSTORE"},
    ...
    {code: "OntologyStore",
        forceLoading: true,
        forceConfigMsg: {params: []},
        forceDir: "cells/plugins/standard"
    }
];
```

For more information on installing the plug-in, please visit [Web Client Plug-in Developers Guide](https://community.i2b2.org/wiki/display/webclient/Web+Client+Plug-in+Developers+Guide).
