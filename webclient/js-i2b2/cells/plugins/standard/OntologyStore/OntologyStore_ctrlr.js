i2b2.OntologyStore.modal = {
    progress: {
        show: function (title) {
            jQuery('#OntologyStore-ProgressModalTitle').text(title);
            if (!i2b2.OntologyStore.modal.progress.panel) {
                let panel = new YAHOO.widget.Panel('OntologyStore-ProgressModal', {
                    width: "200px",
                    fixedcenter: true,
                    close: false,
                    draggable: false,
                    zindex: 100,
                    modal: true,
                    visible: false
                });
                panel.render(document.body);
                i2b2.OntologyStore.modal.progress.panel = panel;
            }

            i2b2.OntologyStore.modal.progress.panel.show();
        },
        hide: function () {
            if (i2b2.OntologyStore.modal.progress.panel) {
                i2b2.OntologyStore.modal.progress.panel.hide();
            }
        }
    },
    message: {
        show: function (title, message) {
            document.getElementById('OntologyStore-MessageModalTitle').innerHTML = title;
            document.getElementById('OntologyStore-MessageModalMessage').innerHTML = message;

            if (!this.panel) {
                let panel = new YAHOO.widget.Panel('OntologyStore-MessageModal', {
                    width: "400px",
                    fixedcenter: true,
                    close: true,
                    draggable: true,
                    zindex: 100,
                    modal: true,
                    visible: false
                });
                panel.render(document.body);
                i2b2.OntologyStore.modal.message.panel = panel;
            }

            i2b2.OntologyStore.modal.message.panel.show();
        },
        hide: function () {
            if (i2b2.OntologyStore.modal.message.panel) {
                i2b2.OntologyStore.modal.message.panel.hide();
            }
        }
    }
};

i2b2.OntologyStore.message = {
    getSummaryProgress: function (summary) {
        if (summary.actionType === 'Download') {
            if (summary.inProgress) {
                return '<span class="ontologystore-text-info"><i class="bi bi-file-earmark-arrow-down"></i> In Progress</span>';
            } else {
                return summary.success
                        ? '<span class="ontologystore-text-success"><i class="bi bi-file-earmark-arrow-down"></i> Success</span>'
                        : '<span class="ontologystore-text-danger"><i class="bi bi-file-earmark-arrow-down"></i> Failed</span>';
            }
        } else {
            if (summary.inProgress) {
                return '<span class="ontologystore-text-info"><i class="bi bi-server"></i> In Progress</span>';
            } else {
                return summary.success
                        ? '<span class="ontologystore-text-success"><i class="bi bi-server"></i> Success</span>'
                        : '<span class="ontologystore-text-danger"><i class="bi bi-server"></i> Failed</span>';
            }
        }

    },
    createSummaryTable: function (data) {
        let table = document.createElement('table');
        table.id = 'OntologyStore-ActionSummary';

        // create headers
        let headers = ['Ontology', 'Action', 'Status', 'Detail'];
        let tHead = table.createTHead();
        let row = tHead.insertRow(-1);
        for (let i = 0; i < headers.length; i++) {
            let th = document.createElement('th');
            th.innerHTML = headers[i];
            row.appendChild(th);
        }

        let tBody = table.createTBody();
        for (let i = 0; i < data.length; i++) {
            let columns = [];
            row = tBody.insertRow(-1);
            for (let i = 0; i < headers.length; i++) {
                columns[i] = row.insertCell(i);
            }

            let summary = data[i];
            columns[0].innerHTML = summary.title;
            columns[1].innerHTML = summary.actionType;
            columns[2].innerHTML = this.getSummaryProgress(summary);
            columns[3].innerHTML = summary.detail;

            columns[2].className = 'text-center';
        }

        return table;
    },
    panel: {},
    show: function (title, message) {
        document.getElementById('OntologyStore-MessageModalTitle').innerHTML = title;
        document.getElementById('OntologyStore-MessageModalMessage').innerHTML = message;

        if (!this.panel.error) {
            var panel = new YAHOO.widget.Panel('OntologyStore-MessageModal', {
                width: '400px',
                fixedcenter: true,
                close: true,
                draggable: true,
                zindex: 100,
                modal: false,
                visible: false
            });
            panel.render(document.body);
            this.panel.error = panel;
        }
        this.panel.error.show();
    },
    showSummary: function (data) {
        document.getElementById('OntologyStore-MessageModalTitle').innerHTML = 'Download/Install Summary';
        document.getElementById('OntologyStore-MessageModalMessage').innerHTML = null;
        document.getElementById('OntologyStore-MessageModalMessage').appendChild(this.createSummaryTable(data));

        if (!this.panel.summary) {
            var panel = new YAHOO.widget.Panel('OntologyStore-MessageModal', {
                width: '800px',
                fixedcenter: true,
                close: true,
                draggable: true,
                zindex: 100,
                modal: true,
                visible: false
            });
            panel.render(document.body);
            this.panel.summary = panel;
        }
        this.panel.summary.show();
    }
};

i2b2.OntologyStore.refreshProductTable = function () {
    let datatable = i2b2.OntologyStore.productTable;
    datatable.clear();
    jQuery.each(i2b2.OntologyStore.products, function (index, product) {
        let columns = [];
        columns[0] = product.title;
        columns[1] = product.version;
        columns[2] = product.owner;
        columns[3] = product.type;
        columns[4] = '';
        columns[5] = '';
        columns[6] = '';
        columns[7] = '';
        columns[8] = '';

        if (product.downloaded) {
            columns[4] = product.includeNetworkPackage
                    ? '<input id="network-' + index + '" type="checkbox" name="network" checked="checked" disabled="disabled" />'
                    : '<input id="network-' + index + '" type="checkbox" name="network" disabled="disabled" />';
        } else {
            columns[4] = product.includeNetworkPackage
                    ? '<input id="network-' + index + '" type="checkbox" name="network" checked="checked" />'
                    : '<input id="network-' + index + '" type="checkbox" name="network" />';
        }

        columns[5] = product.terminologies.join(',');

        if (product.downloaded) {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" disabled="disabled" />';

            if (product.installed) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-success">Installed</span>';
            } else if (product.failed) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-danger">Installation Failed</span>';
            } else if (product.started) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-info">Installation In Progress</span>';
            } else {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" />';
                columns[8] = '<span class="ontologystore-text-warning">Ready To Be Installed</span>';
            }
        } else if (product.failed) {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" disabled="disabled" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontologystore-text-danger">Download Failed</span>';
        } else if (product.started) {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" disabled="disabled" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontologystore-text-info">Download In Progress</span>';
        } else {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" />';
        }

        datatable.row.add(columns);
    });
    datatable.draw();
};

i2b2.OntologyStore.syncFromCloud = function () {
    i2b2.OntologyStore.modal.progress.show('Sync From Clould');

    var scopedCallback = new i2b2_scopedCallback();
    scopedCallback.callback = function (results) {
        if (results.error) {
            var errorMsg = results.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
            document.getElementById("OntologyStore-ExecuteBtn").disabled = true;
            i2b2.OntologyStore.modal.progress.hide();
            i2b2.OntologyStore.modal.message.show('Sync From Cloud Failed', errorMsg);
        } else {
            i2b2.OntologyStore.products = results.parse();
            i2b2.OntologyStore.refreshProductTable();

            document.getElementById("OntologyStore-ExecuteBtn").disabled = false;
            i2b2.OntologyStore.modal.progress.hide();
            jQuery('#OntologyStore-Product').show();
        }
    };
    i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, scopedCallback);
};

i2b2.OntologyStore.getSelectedProductIndexes = function () {
    let indexes = [];

    let index = 0;
    let selections = document.querySelectorAll('input[name="download"]:checked');
    for (let i = 0; i < selections.length; i++) {
        indexes[index++] = selections[i].dataset.id;
    }

    selections = document.querySelectorAll('input[name="install"]:checked');
    for (let i = 0; i < selections.length; i++) {
        indexes[index++] = selections[i].dataset.id;
    }

    // get unique ids
    indexes = indexes.filter(function (value, index, self) {
        return self.indexOf(value) === index;
    });

    return indexes;
};

i2b2.OntologyStore.getSelectedProducts = function (products) {
    let data = [];

    jQuery.each(i2b2.OntologyStore.getSelectedProductIndexes(), function (index, productIndex) {
        let product = products[productIndex];
        let includeNetChkbx = document.getElementById('network-' + productIndex);
        let downloadChkbx = document.getElementById('download-' + productIndex);
        let installChkbx = document.getElementById('install-' + productIndex);

        data[index] = {
            title: product.title,
            key: product.fileName,
            includeNetworkPackage: includeNetChkbx.checked,
            download: downloadChkbx.checked,
            install: installChkbx.checked
        };
    });

    return data;
};

i2b2.OntologyStore.execute = function () {
    var products = i2b2.OntologyStore.products;
    if (products && products.length > 0) {
        if (i2b2.PM.model.isAdmin) {
            var selectedProducts = i2b2.OntologyStore.getSelectedProducts(products);
            if (selectedProducts.length > 0) {
                let executeBtn = document.getElementById("OntologyStore-ExecuteBtn");
                executeBtn.disabled = true;

                i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');

                let options = {
                    version: i2b2.ClientVersion,
                    products_str_xml: i2b2.OntologyStore.productsToXml(selectedProducts)
                };
                var scopedCallback = new i2b2_scopedCallback();
                scopedCallback.callback = function (productActionResults) {
                    executeBtn.disabled = false;
                    if (productActionResults.error) {
                        var innerScopedCallback = new i2b2_scopedCallback();
                        innerScopedCallback.callback = function (getProductResults) {
                            if (!getProductResults.error) {
                                i2b2.OntologyStore.products = getProductResults.parse();
                                i2b2.OntologyStore.refreshProductTable();
                            }

                            i2b2.OntologyStore.modal.progress.hide();

                            var errorMsg = productActionResults.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
                            i2b2.OntologyStore.modal.message.show("Download/Install Ontology Failed", errorMsg);
                        };
                        i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, innerScopedCallback);
                    } else {
                        var innerScopedCallback = new i2b2_scopedCallback();
                        innerScopedCallback.callback = function (getProductResults) {
                            if (!getProductResults.error) {
                                i2b2.OntologyStore.products = getProductResults.parse();
                                i2b2.OntologyStore.refreshProductTable();
                            }

                            var data = productActionResults.parse();

                            // check to see if refresh is required    
                            for (let i = 0; i < data.length; i++) {
                                if (data[i].actionType === 'Install') {
                                    i2b2.ONT.view.nav.doRefreshAll();
                                    break;
                                }
                            }


                            i2b2.OntologyStore.modal.progress.hide();
                            i2b2.OntologyStore.message.showSummary(data);
                        };
                        i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, innerScopedCallback);
                    }
                };
                i2b2.ONTSTORE.ajax.PerformProductActions("OntologyStore Plugin", options, scopedCallback);
            } else {
                // at least one ontology must be selected to download/install.
                i2b2.OntologyStore.modal.message.show('No Ontology Selected', 'Please select an ontology to download/install.');
            }
        } else {
            // must be admin to download/install ontology
            i2b2.OntologyStore.modal.message.show('Insufficient Privileges', 'Administrative privileges required!');
        }
    }
}

i2b2.OntologyStore.productToXml = function (product) {
    var tags = [];
    tags.push('            <product_action>');
    tags.push('                <title>' + product.title + '</title>');
    tags.push('                <key>' + product.key + '</key>');
    tags.push('                <include_network_package>' + product.includeNetworkPackage + '</include_network_package>');
    tags.push('                <download>' + product.download + '</download>');
    tags.push('                <install>' + product.install + '</install>');
    tags.push('            </product_action>');

    return tags.join('\n');
};

i2b2.OntologyStore.productsToXml = function (products) {
    var xml = [];
    for (var i = 0; i < products.length; i++) {
        xml.push(i2b2.OntologyStore.productToXml(products[i]));
    }

    return xml.join('\n');
};

i2b2.OntologyStore.Init = function (loadedDiv) {
    i2b2.OntologyStore.productTable = jQuery('#OntologyStore-ProductTable').DataTable({
        'columnDefs': [
            {'targets': 0, 'className': 'ontologystore-title'},
            {'targets': 4, 'className': 'dt-center ontologystore-network-chkbx'},
            {'targets': 6, 'className': 'dt-center ontologystore-download-chkbx'},
            {'targets': 7, 'className': 'dt-center ontologystore-install-chkbx'}
        ]
    });

    document.getElementById('OntologyStore-ExecuteBtn').disabled = true;
};

i2b2.OntologyStore.Unload = function () {
    i2b2.OntologyStore.products = [];

    document.getElementById('OntologyStore-ExecuteBtn').disabled = true;

    return true;
};