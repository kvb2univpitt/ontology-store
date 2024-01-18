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
                return '<span class="ontstore-bs-text-info"><i class="bi bi-file-earmark-arrow-down"></i> In Progress</span>';
            } else {
                return summary.success
                        ? '<span class="ontstore-bs-text-success"><i class="bi bi-file-earmark-arrow-down"></i> Success</span>'
                        : '<span class="ontstore-bs-text-danger"><i class="bi bi-file-earmark-arrow-down"></i> Failed</span>';
            }
        } else {
            if (summary.inProgress) {
                return '<span class="ontstore-bs-text-info"><i class="bi bi-server"></i> In Progress</span>';
            } else {
                return summary.success
                        ? '<span class="ontstore-bs-text-success"><i class="bi bi-server"></i> Success</span>'
                        : '<span class="ontstore-bs-text-danger"><i class="bi bi-server"></i> Failed</span>';
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
        columns[9] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" disabled="disabled" />';

        columns[4] = product.includeNetworkPackage
                ? '<input type="checkbox" class="ontstore-bs-form-check-input" id="network-' + index + '" name="network" disabled="disabled" checked="checked" />'
                : '<input type="checkbox" class="ontstore-bs-form-check-input" id="network-' + index + '" name="network" disabled="disabled" />';

        columns[5] = product.terminologies.join(',');

        if (product.downloaded) {
            columns[6] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';

            if (product.installed) {
                columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontstore-bs-text-success ontstore-bs-font-weight-bold">Installed</span>';

                if (product.disabled) {
                    columns[9] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" checked="checked" />';
                } else {
                    columns[9] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" />';
                }
            } else if (product.failed) {
                columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontstore-bs-text-danger ontstore-bs-font-weight-bold">Installation Failed</span>';
            } else if (product.started) {
                columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="ontstore-bs-text-info ontstore-bs-font-weight-bold">Installation In Progress</span>';
            } else {
                columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" onclick="i2b2.OntologyStore.installCheckboxAction(' + index + ')" />';
                columns[8] = '<span class="ontstore-bs-text-warning ontstore-bs-font-weight-bold">Ready To Be Installed</span>';
            }
        } else if (product.failed) {
            columns[6] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontstore-bs-text-danger ontstore-bs-font-weight-bold">Download Failed</span>';
        } else if (product.started) {
            columns[6] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" disabled="disabled" />';
            columns[8] = '<span class="ontstore-bs-text-info ontstore-bs-font-weight-bold">Download In Progress</span>';
        } else {
            columns[6] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" onclick="i2b2.OntologyStore.downloadCheckboxAction(' + index + ')" />';
            columns[7] = '<input type="checkbox" class="ontstore-bs-form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" onclick="i2b2.OntologyStore.installCheckboxAction(' + index + ')" />';
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
            jQuery('#OntologyStore-ExecuteBtn').prop("disabled", true);
            i2b2.OntologyStore.modal.progress.hide();
            i2b2.OntologyStore.modal.message.show('Sync From Cloud Failed', errorMsg);
            console.log(errorMsg);
        } else {
            i2b2.OntologyStore.products = results.parse();
            i2b2.OntologyStore.refreshProductTable();

            jQuery('#OntologyStore-ExecuteBtn').prop("disabled", (i2b2.OntologyStore.products.length === 0));
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
        let downloadChkbx = document.getElementById('download-' + productIndex);
        let installChkbx = document.getElementById('install-' + productIndex);
        let disableChkbx = document.getElementById('disable-' + productIndex);

        data.push(
                {
                    id: product.id,
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
    tags.push('                <id>' + product.id + '</id>');
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
                jQuery('#OntologyStore-ExecuteBtn').prop("disabled", true);
                i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');

                let options = {
                    result_wait_time: -1,
                    version: i2b2.ClientVersion,
                    products_str_xml: i2b2.OntologyStore.productsToXml(selectedProducts)
                };
                let scopedCallback = new i2b2_scopedCallback();
                scopedCallback.callback = function (productActionResults) {
                    setTimeout(function () {
                        let innerScopedCallback = new i2b2_scopedCallback();
                        innerScopedCallback.callback = function (getProductResults) {
                            if (!getProductResults.error) {
                                i2b2.OntologyStore.products = getProductResults.parse();
                                i2b2.OntologyStore.refreshProductTable();
                            }

                            if (productActionResults.error) {
                                let msgTitle = '';
                                let msgBody = '';
                                if (productActionResults.msgResponse.includes('504 Gateway Timeout')) {
                                    msgTitle = 'Request Timeout';
                                    msgBody = 'The current request takes longer than normal.'
                                            + '  If set to installed, the ontologies will appear in the "Terms" panel the next time you log back in after the installation is done.'
                                } else {
                                    msgTitle = 'Download/Install Ontology Failed';
                                    try {
                                        msgBody = productActionResults.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
                                    } catch (exception) {
                                        msgBody = 'Server error: ' + productActionResults.errorMsg;
                                    }
                                }
                                i2b2.OntologyStore.modal.message.show(msgTitle, msgBody);

                                jQuery('#OntologyStore-ExecuteBtn').prop("disabled", false);
                                i2b2.OntologyStore.modal.progress.hide();
                            } else {
                                let data = productActionResults.parse();

                                // check to see if refresh is required    
                                for (let i = 0; i < data.length; i++) {
                                    if ((data[i].actionType === 'Install') || (data[i].actionType === 'Enable') || (data[i].actionType === 'Disable')) {
                                        i2b2.ONT.view.nav.doRefreshAll();
                                        break;
                                    }
                                }

                                jQuery('#OntologyStore-ExecuteBtn').prop("disabled", false);
                                i2b2.OntologyStore.modal.progress.hide();
                                i2b2.OntologyStore.modal.summary.show(data);
                            }
                        };
                        i2b2.ONTSTORE.ajax.GetProducts("OntologyStore Plugin", {version: i2b2.ClientVersion}, innerScopedCallback);
                    }, 500);
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
            {'targets': 0, 'className': 'ontstore-title'},
            {'targets': 4, 'className': 'ontstore-bs-text-center ontstore-network-chkbx', 'orderable': false},
            {'targets': 6, 'className': 'ontstore-bs-text-center ontstore-download-chkbx', 'orderable': false},
            {'targets': 7, 'className': 'ontstore-bs-text-center ontstore-install-chkbx', 'orderable': false},
            {'targets': 9, 'className': 'ontstore-bs-text-center ontstore-disable-chkbx', 'orderable': false}
        ]
    });

    i2b2.OntologyStore.products = [];

    jQuery('#OntologyStore-SyncFromCloud').click(function () {
        i2b2.OntologyStore.syncFromCloud();
    });
    jQuery('#OntologyStore-ExecuteBtn').click(function () {
        i2b2.OntologyStore.execute();
    });
    jQuery('#OntologyStore-ExecuteBtn').prop("disabled", true);

    i2b2.OntologyStore.syncFromCloud();
};

i2b2.OntologyStore.Unload = function () {
    i2b2.OntologyStore.products = [];

    jQuery('#OntologyStore-ExecuteBtn').prop("disabled", true);

    return true;
};