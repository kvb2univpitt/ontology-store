i2b2.OntologyStore.productTable = null;
i2b2.OntologyStore.products = [];

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
                    zindex: 4,
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
            if (!i2b2.OntologyStore.modal.message.panel) {
                jQuery('#OntologyStore-MessageModalTitle').text(title);
                jQuery('#OntologyStore-MessageModalMessage').text(message);
                let panel = new YAHOO.widget.Panel('OntologyStore-MessageModal', {
                    width: "400px",
                    fixedcenter: true,
                    close: true,
                    draggable: true,
                    zindex: 4,
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
    createSummaryTable: function (data) {
        let table = document.createElement('table');
        table.id = 'OntologyStore-ActionSummary';

        // create headers
        let headers = ["Ontology", "Action", "Status", "Detail"];
        let tHead = table.createTHead();
        let row = tHead.insertRow(-1);
        for (let i = 0; i < headers.length; i++) {
            let th = document.createElement('th');
            th.innerHTML = headers[i];
            row.appendChild(th);
        }

        let tBody = table.createTBody()
        for (let i = 0; i < data.length; i++) {
            let columns = [];
            row = tBody.insertRow(-1);
            for (let i = 0; i < headers.length; i++) {
                columns[i] = row.insertCell(i);
            }

            let summary = data[i];
            columns[0].innerHTML = summary.title;
            columns[1].innerHTML = summary.actionType;
            columns[2].innerHTML = summary.inProgress ? '<span class="text-danger">In Progress</span>' : summary.success ? '<span class="text-success">Success</span>' : '<span class="text-fail">Failed</span>';
            columns[3].innerHTML = summary.detail;

            columns[2].className = "text-center";
        }

        return table;
    },
    panel: {},
    show: function (title, message) {
        document.getElementById("OntologyStore-MessageModalTitle").innerHTML = title;
        document.getElementById("OntologyStore-MessageModalMessage").innerHTML = message;

        if (!this.panel.error) {
            let panel = new YAHOO.widget.Panel("OntologyStore-MessageModal", {
                width: "400px",
                fixedcenter: true,
                close: true,
                draggable: true,
                zindex: 4,
                modal: true,
                visible: false
            });
            panel.render(document.body);
            this.panel.error = panel;
        }
        this.panel.error.show();
    },
    showSummary: function (data) {
        document.getElementById("OntologyStore-MessageModalTitle").innerHTML = "Download/Install Summary";
        document.getElementById("OntologyStore-MessageModalMessage").innerHTML = null;
        document.getElementById("OntologyStore-MessageModalMessage").appendChild(this.createSummaryTable(data));

        if (!this.panel.summary) {
            let panel = new YAHOO.widget.Panel("OntologyStore-MessageModal", {
                width: "600px",
                fixedcenter: true,
                close: true,
                draggable: true,
                zindex: 4,
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
        columns[4] = product.type;
        columns[5] = product.type;
        columns[6] = product.type;
        columns[7] = product.type;
        columns[8] = product.type;

        if (product.downloaded) {
            columns[4] = product.includeNetworkPackage
                    ? `<input id="network-${index}" type="checkbox" name="network" checked="checked" disabled="disabled" />`
                    : `<input id="network-${index}" type="checkbox" name="network" disabled="disabled" />`;
        } else {
            columns[4] = product.includeNetworkPackage
                    ? `<input id="network-${index}" type="checkbox" name="network" checked="checked" />`
                    : `<input id="network-${index}" type="checkbox" name="network" />`;
        }

        columns[5] = product.terminologies.join(',');

        if (product.downloaded) {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;

            if (product.installed) {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8] = '<span class="text-success">Completed</span>';
            } else if (product.failed) {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8] = '<span class="text-danger">Installation Failed</span>';
            } else if (product.started) {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8] = '<span class="text-info">Installation In Progress</span>';
            } else {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
                columns[8] = 'Ready To Be Installed';
            }
        } else if (product.failed) {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
            columns[8] = '<span class="text-danger">Download Failed</span>';
        } else if (product.started) {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
            columns[8] = '<span class="text-info">Download In Progress</span>';
        } else {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
        }

        datatable.row.add(columns);
    });
    datatable.draw();
};

i2b2.OntologyStore.fetchProducts = function () {
    jQuery.ajax({
        type: 'GET',
        dataType: 'text',
        url: 'http://' + location.host + '/ontology-store/products'
    }).success(function (data) {
        i2b2.OntologyStore.products = JSON.parse(data);
        i2b2.OntologyStore.refreshProductTable();
    });
};

i2b2.OntologyStore.syncFromCloud = function () {
    i2b2.OntologyStore.modal.progress.show('Sync From Clould');
    jQuery.ajax({
        type: 'GET',
        dataType: 'text',
        url: 'http://' + location.host + '/ontology-store/products'
    }).success(function (data) {
        i2b2.OntologyStore.products = JSON.parse(data);
        i2b2.OntologyStore.refreshProductTable();

        document.getElementById("OntologyStore-ExecuteBtn").disabled = false;
        i2b2.OntologyStore.modal.progress.hide();
    }).error(function () {
        document.getElementById("OntologyStore-ExecuteBtn").disabled = true;
        i2b2.OntologyStore.modal.progress.hide();
        i2b2.OntologyStore.modal.message.show('Save Phenotype Failed', 'Unable to save phenotype workbook at this time.');
    });
};

i2b2.OntologyStore.execute = function () {
    let products = i2b2.OntologyStore.products;
    if (products && products.length > 0) {
        let indexes = [];
        document.querySelectorAll('input[name="download"]:checked').forEach(chkbx => {
            indexes.push(chkbx.dataset.id);
        });
        document.querySelectorAll('input[name="install"]:checked').forEach(chkbx => {
            indexes.push(chkbx.dataset.id);
        });

        // get unique ids
        indexes = indexes.filter((value, index, self) => {
            return self.indexOf(value) === index;
        });


        let data = [];
        indexes.forEach(index => {
            let product = products[index];
            let includeNetChkbx = document.getElementById(`network-${index}`);
            let downloadChkbx = document.getElementById(`download-${index}`);
            let installChkbx = document.getElementById(`install-${index}`);

            data.push({
                title: product.title,
                key: product.fileName,
                includeNetworkPackage: includeNetChkbx.checked,
                download: downloadChkbx.checked,
                install: installChkbx.checked
            });
        });

        if (data.length > 0) {
            if (i2b2.PM.model.isAdmin) {
                let executeBtn = document.getElementById("OntologyStore-ExecuteBtn");

                executeBtn.disabled = true;
                i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');

                jQuery.ajax({
                    type: 'POST',
                    headers: {
                        'X-I2B2-Domain': i2b2.h.getDomain(),
                        'X-I2B2-Project': i2b2.h.getProject(),
                        'Authorization': 'Basic ' + btoa(i2b2.h.getUser() + ':' + i2b2.h.getPass())
                    },
                    url: 'http://' + location.host + '/ontology-store/action',
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(data)
                }).fail(function (data) {
                    executeBtn.disabled = false;
                    i2b2.OntologyStore.modal.progress.hide();
                    i2b2.OntologyStore.fetchProducts();

                    i2b2.OntologyStore.message.show(data.statusText, data.responseText);
                }).success(function (data) {
                    executeBtn.disabled = false;
                    i2b2.OntologyStore.modal.progress.hide();
                    i2b2.OntologyStore.fetchProducts();

                    i2b2.OntologyStore.message.showSummary(data);
                });
            } else {
                i2b2.OntologyStore.message.show('Insufficient Privileges', 'Administrative privileges required!');
            }
        } else {
            i2b2.OntologyStore.message.show('No Ontology Selected', 'Please select an ontology to download/install.');
        }
    }
};

i2b2.OntologyStore.Init = function (loadedDiv) {
    i2b2.OntologyStore.productTable = jQuery('#OntologyStore-ProductTable').DataTable({
        columnDefs: [
            {
                "targets": 4,
                "className": "text-center"
            },
            {
                "targets": 6,
                "className": "text-center"
            },
            {
                "targets": 7,
                "className": "text-center"
            }
        ]
    });

    document.getElementById("OntologyStore-ExecuteBtn").disabled = true;
};

i2b2.OntologyStore.Unload = function () {
    i2b2.OntologyStore.products = [];

    document.getElementById("OntologyStore-ExecuteBtn").disabled = true;

    return true;
};
