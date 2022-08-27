{
    files: ["OntologyStore_ctrlr.js", "datatables.min.js"],
    css: ["OntologyStore.css", "OntologyStore_bootstrap.css", "bootstrap-icons.css", "datatables.min.css"],
    config: {
        // Additional configuration variables that are set by the system
        short_name: "Ontology Store",
        name: "Ontology Store",
        description: "This plugin enables users to download and install ontologies.",
        icons: {size32x32: "OntologyStore_icon_32x32.png"},
        category: ["celless", "plugin", "standard"],
        plugin: {
            isolateHtml: false, // This means do not use an IFRAME
            isolateComm: true, // This means to expect the plugin to use AJAX communications provided by the framework
            html: {
                source: 'injected_screens.html',
                mainDivId: 'OntologyStore-mainDiv'
            }
        }
    }
}