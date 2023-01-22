i2b2.OntologyStore.modal = {
    progress: {
        show: function (title) {
            jQuery('#OntologyStore-ProgressModalTitle').text(title);

            if (!i2b2.OntologyStore.modal.progress.panel) {
                i2b2.OntologyStore.modal.progress.panel = new YAHOO.widget.Panel('OntologyStore-ProgressModal', {
                    width: "200px",
                    fixedcenter: true,
                    close: false,
                    draggable: false,
                    zindex: 100,
                    modal: true,
                    visible: false
                });
                i2b2.OntologyStore.modal.progress.panel.render(document.body);
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

            if (!i2b2.OntologyStore.modal.message.panel) {
                i2b2.OntologyStore.modal.message.panel = new YAHOO.widget.Panel('OntologyStore-MessageModal', {
                    width: "400px",
                    fixedcenter: true,
                    close: false,
                    draggable: true,
                    zindex: 100,
                    modal: true,
                    visible: false
                });
                i2b2.OntologyStore.modal.message.panel.render(document.body);
            }

            i2b2.OntologyStore.modal.message.panel.show();
        },
        hide: function () {
            if (i2b2.OntologyStore.modal.message.panel) {
                i2b2.OntologyStore.modal.message.panel.hide();
            }
        }
    },
    summary: {
        show: function (data) {
            document.getElementById('OntologyStore-SummaryMessageModalMessage').innerHTML = null;
            document.getElementById('OntologyStore-SummaryMessageModalMessage').appendChild(i2b2.OntologyStore.summary.createSummaryTable(data));

            if (!i2b2.OntologyStore.modal.summary.panel) {
                i2b2.OntologyStore.modal.summary.panel = new YAHOO.widget.Panel('OntologyStore-SummaryMessageModal', {
                    width: '800px',
                    fixedcenter: true,
                    close: false,
                    draggable: true,
                    zindex: 100,
                    modal: true,
                    visible: false
                });
                i2b2.OntologyStore.modal.summary.panel.render(document.body);
            }

            i2b2.OntologyStore.modal.summary.panel.show();
        },
        hide: function () {
            if (i2b2.OntologyStore.modal.summary.panel) {
                i2b2.OntologyStore.modal.summary.panel.hide();
            }
        }
    }
};

i2b2.OntologyStore.summary = {
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
    }
};

i2b2.OntologyStore.downloadCheckboxAction = function (productIndex) {
    let installChkbx = document.getElementById('install-' + productIndex);
    let downloadChkbx = document.getElementById('download-' + productIndex);

    if (!downloadChkbx.checked) {
        installChkbx.checked = false;
    }
};

i2b2.OntologyStore.installCheckboxAction = function (productIndex) {
    let installChkbx = document.getElementById('install-' + productIndex);
    let downloadChkbx = document.getElementById('download-' + productIndex);
    let disableChkbx = document.getElementById('disable-' + productIndex);

    if (installChkbx.checked) {
        downloadChkbx.checked = true;
    } else {
        disableChkbx.checked = false;
    }

    disableChkbx.disabled = !installChkbx.checked;
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
        columns[9] = '<input id="disable-' + index + '" data-id="' + index + '" type="checkbox" name="disable" disabled="disabled" />';

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
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" checked="checked" disabled="disabled" />';

            if (product.installed) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-success">Installed</span>';

                if (product.disabled) {
                    columns[9] = '<input id="disable-' + index + '" data-id="' + index + '" type="checkbox" name="disable" checked="checked" />';
                } else {
                    columns[9] = '<input id="disable-' + index + '" data-id="' + index + '" type="checkbox" name="disable" />';
                }
            } else if (product.failed) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-danger">Installation Failed</span>';
            } else if (product.started) {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontologystore-text-info">Installation In Progress</span>';
            } else {
                columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" onclick="i2b2.OntologyStore.installCheckboxAction(' + index + ')" />';
                columns[8] = '<span class="ontologystore-text-warning">Ready To Be Installed</span>';
            }
        } else if (product.failed) {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontologystore-text-danger">Download Failed</span>';
        } else if (product.started) {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontologystore-text-info">Download In Progress</span>';
        } else {
            columns[6] = '<input id="download-' + index + '" data-id="' + index + '" type="checkbox" name="download" onclick="i2b2.OntologyStore.downloadCheckboxAction(' + index + ')" />';
            columns[7] = '<input id="install-' + index + '" data-id="' + index + '" type="checkbox" name="install" onclick="i2b2.OntologyStore.installCheckboxAction(' + index + ')" />';
        }

        datatable.row.add(columns);
    });
    datatable.draw();
};

i2b2.OntologyStore.syncFromCloud = function () {
    i2b2.OntologyStore.modal.progress.show('Sync From Clould');

    let scopedCallback = new i2b2_scopedCallback();
    scopedCallback.callback = function (results) {
        if (results.error) {
            let errorMsg;
            try {
                errorMsg = results.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
            } catch (exception) {
                errorMsg = 'Server error: ' + results.errorMsg;
            }
            document.getElementById("OntologyStore-ExecuteBtn").disabled = true;
            i2b2.OntologyStore.modal.progress.hide();
            i2b2.OntologyStore.modal.message.show('Sync From Cloud Failed', errorMsg);
            console.log(errorMsg);
        } else {
            i2b2.OntologyStore.products = results.parse();
            i2b2.OntologyStore.refreshProductTable();

            document.getElementById("OntologyStore-ExecuteBtn").disabled = (i2b2.OntologyStore.products.length === 0);
            i2b2.OntologyStore.modal.progress.hide();
            jQuery('#OntologyStore-Product').show();
        }
    };
    i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, scopedCallback);
};

i2b2.OntologyStore.getSelectedProductIndexes = function (products) {
    let index = 0;
    let indexes = [];

    // download actions
    let selections = document.querySelectorAll('input[name="download"]:checked:not(:disabled)');
    for (let i = 0; i < selections.length; i++) {
        indexes[index++] = selections[i].dataset.id;
    }

    // install actions
    selections = document.querySelectorAll('input[name="install"]:checked:not(:disabled)');
    for (let i = 0; i < selections.length; i++) {
        indexes[index++] = selections[i].dataset.id;
    }

    // disable actions
    selections = document.querySelectorAll('input[name="disable"]:not(:disabled)');
    for (let i = 0; i < selections.length; i++) {
        let chkbox = selections[i];
        let productIndex = chkbox.dataset.id;
        let product = products[productIndex];
        if (!(product.disabled === chkbox.checked)) {
            indexes[index++] = productIndex;
        }
    }

    // get unique ids
    indexes = indexes.filter(function (value, index, self) {
        return self.indexOf(value) === index;
    });

    return indexes;
};

i2b2.OntologyStore.getSelectedProducts = function (products) {
    let data = [];

    let selectedProductIndexes = i2b2.OntologyStore.getSelectedProductIndexes(products);
    for (let i = 0; i < selectedProductIndexes.length; i++) {
        let productIndex = selectedProductIndexes[i];
        let product = products[productIndex];
        let includeNetChkbx = document.getElementById('network-' + productIndex);
        let downloadChkbx = document.getElementById('download-' + productIndex);
        let installChkbx = document.getElementById('install-' + productIndex);
        let disableChkbx = document.getElementById('disable-' + productIndex);

        data.push(
                {
                    title: product.title,
                    key: product.fileName,
                    includeNetworkPackage: includeNetChkbx.checked,
                    download: downloadChkbx.disabled ? false : downloadChkbx.checked,
                    install: installChkbx.disabled ? false : installChkbx.checked,
                    disableEnable: !(product.disabled === disableChkbx.checked)
                }
        );
    }

    return data;
};

i2b2.OntologyStore.productToXml = function (product) {
    let tags = [];

    tags.push('            <product_action>');
    tags.push('                <title>' + product.title + '</title>');
    tags.push('                <key>' + product.key + '</key>');
    tags.push('                <include_network_package>' + product.includeNetworkPackage + '</include_network_package>');
    tags.push('                <download>' + product.download + '</download>');
    tags.push('                <install>' + product.install + '</install>');
    tags.push('                <disable_enable>' + product.disableEnable + '</disable_enable>');
    tags.push('            </product_action>');

    return tags.join('\n');
};

i2b2.OntologyStore.productsToXml = function (products) {
    let xml = [];
    for (let i = 0; i < products.length; i++) {
        xml.push(i2b2.OntologyStore.productToXml(products[i]));
    }

    return xml.join('\n');
};

i2b2.OntologyStore.execute = function () {
    let products = i2b2.OntologyStore.products;
    if (products && products.length > 0) {
        if (i2b2.PM.model.isAdmin) {
            let selectedProducts = i2b2.OntologyStore.getSelectedProducts(products);
            if (selectedProducts.length > 0) {
                let executeBtn = document.getElementById("OntologyStore-ExecuteBtn");
                executeBtn.disabled = true;

                i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');

                let options = {
                    version: i2b2.ClientVersion,
                    products_str_xml: i2b2.OntologyStore.productsToXml(selectedProducts)
                };
                let scopedCallback = new i2b2_scopedCallback();
                scopedCallback.callback = function (productActionResults) {
                    executeBtn.disabled = false;
                    if (productActionResults.error) {
                        let innerScopedCallback = new i2b2_scopedCallback();
                        innerScopedCallback.callback = function (getProductResults) {
                            if (!getProductResults.error) {
                                i2b2.OntologyStore.products = getProductResults.parse();
                                i2b2.OntologyStore.refreshProductTable();
                            }

                            let errorMsg;
                            try {
                                errorMsg = productActionResults.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
                            } catch (exception) {
                                errorMsg = 'Server error: ' + productActionResults.errorMsg;
                            }
                            i2b2.OntologyStore.modal.progress.hide();
                            i2b2.OntologyStore.modal.message.show("Download/Install Ontology Failed", errorMsg);
                            console.log(errorMsg);
                        };
                        i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, innerScopedCallback);
                    } else {
                        let innerScopedCallback = new i2b2_scopedCallback();
                        innerScopedCallback.callback = function (getProductResults) {
                            if (!getProductResults.error) {
                                i2b2.OntologyStore.products = getProductResults.parse();
                                i2b2.OntologyStore.refreshProductTable();
                            }

                            let data = productActionResults.parse();

                            // check to see if refresh is required    
                            for (let i = 0; i < data.length; i++) {
                                if ((data[i].actionType === 'Install') || (data[i].actionType === 'Enable') || (data[i].actionType === 'Disable')) {
                                    i2b2.ONT.view.nav.doRefreshAll();
                                    break;
                                }
                            }

                            i2b2.OntologyStore.modal.progress.hide();
                            i2b2.OntologyStore.modal.summary.show(data);
                        };
                        i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, innerScopedCallback);
                    }
                };
                i2b2.ONTSTORE.ajax.PerformProductActions("OntologyStore Plugin", options, scopedCallback);
            } else {
                // at least one ontology must be selected to download/install.
                i2b2.OntologyStore.modal.message.show('No Ontology Selected', 'Please select an ontology to download, install or disable/enable.');
            }
        } else {
            // must be admin to download/install ontology
            i2b2.OntologyStore.modal.message.show('Insufficient Privileges', 'Administrative privileges required!');
        }
    }
};

i2b2.OntologyStore.Init = function (loadedDiv) {
    i2b2.OntologyStore.productTable = jQuery('#OntologyStore-ProductTable').DataTable({
        'columnDefs': [
            {'targets': 0, 'className': 'ontologystore-title'},
            {'targets': 4, 'className': 'dt-center ontologystore-network-chkbx'},
            {'targets': 6, 'className': 'dt-center ontologystore-download-chkbx'},
            {'targets': 7, 'className': 'dt-center ontologystore-install-chkbx'},
            {'targets': 9, 'className': 'dt-center ontologystore-disable-chkbx'}
        ]
    });

    document.getElementById('OntologyStore-ExecuteBtn').disabled = true;
    i2b2.OntologyStore.syncFromCloud();
};

i2b2.OntologyStore.Unload = function () {
    i2b2.OntologyStore.products = [];

    document.getElementById('OntologyStore-ExecuteBtn').disabled = true;

    return true;
};