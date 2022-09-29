i2b2.OntologyStore = {
    env: {
        isAdmin: false,
        project: '',
        domain: '',
        user: '',
        session: ''
    },
    func: {
        doRefreshAll: function () {
            i2b2.authorizedTunnel.function["i2b2.ONT.view.nav.doRefreshAll"]().then((doRefreshAll) => {
                doRefreshAll();
            });
        }
    }
};

i2b2.OntologyStore.productTable = jQuery('table#OntologyStore-ProductTable').DataTable({
    "columnDefs": [
        {"targets": 0, "className": "ontologystore-title"},
        {"targets": 4, "className": "dt-center ontologystore-network-chkbx"},
        {"targets": 6, "className": "dt-center ontologystore-download-chkbx"},
        {"targets": 7, "className": "dt-center ontologystore-install-chkbx"}
    ]
});

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
                columns[8] = '<span class="ontologystore-text-success">Installed</span>';
            } else if (product.failed) {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8] = '<span class="ontologystore-text-danger">Installation Failed</span>';
            } else if (product.started) {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8] = '<span class="ontologystore-text-info">Installation In Progress</span>';
            } else {
                columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
                columns[8] = '<span class="ontologystore-text-warning">Ready To Be Installed</span>';
            }
        } else if (product.failed) {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
            columns[8] = '<span class="ontologystore-text-danger">Download Failed</span>';
        } else if (product.started) {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
            columns[8] = '<span class="ontologystore-text-info">Download In Progress</span>';
        } else {
            columns[6] = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" />`;
            columns[7] = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
        }

        datatable.row.add(columns);
    });
    datatable.draw();
};

i2b2.OntologyStore.modal = {
    progress: {
        show: (title) => {
            $('#OntologyStore-ProgressModalTitle').text(title);
            $('#OntologyStore-ProgressModal').modal('show');
        },
        hide: () => {
            $('#OntologyStore-ProgressModal').modal('hide');
        }
    },
    message: {
        show: (title, message) => {
            $('#OntologyStore-MessageModalLabel').text(title);
            $('#OntologyStore-MessageModalMessage').text(message);
            $('#OntologyStore-MessageModal').modal('show');
        }
    },
    summary: {
        numOfHeaders: 4,
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
        show: function (data) {
            // clear summary table
            $('table#OntologyStore-SummaryTable tbody').empty();

            // add data to summary table
            let table = document.getElementById('OntologyStore-SummaryTable');
            let tBody = (table.tBodies.length > 0) ? table.tBodies[0] : table.createTBody();
            for (let i = 0; i < data.length; i++) {
                // create row with columns
                let columns = [];
                let row = tBody.insertRow(-1);
                for (let i = 0; i < i2b2.OntologyStore.modal.summary.numOfHeaders; i++) {
                    columns[i] = row.insertCell(i);
                }

                let summary = data[i];
                columns[0].innerHTML = summary.title;
                columns[1].innerHTML = summary.actionType;
                columns[2].innerHTML = this.getSummaryProgress(summary);
                columns[3].innerHTML = summary.detail;

                columns[2].className = "text-center";
            }

            $('#OntologyStore-SummaryModal').modal('show');
        }
    }
};

i2b2.OntologyStore.syncFromCloudAction = (successHandler, errorHandler) => {
    $.ajax({
        type: 'GET',
        dataType: 'text',
        url: 'http://' + location.host + '/ontology-store/products',
        success: successHandler,
        error: errorHandler
    });
};

i2b2.OntologyStore.syncFromCloud = () => {
    i2b2.OntologyStore.modal.progress.show('Sync From Clould');
    i2b2.OntologyStore.syncFromCloudAction(
            i2b2.OntologyStore.syncFromCloud.successHandler,
            i2b2.OntologyStore.syncFromCloud.errorHandler);
};

i2b2.OntologyStore.syncFromCloud.successHandler = (data) => {
    setTimeout(function () {
        i2b2.OntologyStore.products = JSON.parse(data);
        i2b2.OntologyStore.refreshProductTable();

        $('#OntologyStore-ExecuteBtn').removeAttr('disabled');
        $('#OntologyStore-Products').show();
        i2b2.OntologyStore.modal.progress.hide();
    }, 500);
};

i2b2.OntologyStore.syncFromCloud.errorHandler = () => {
    setTimeout(function () {
        $('table#OntologyStore-ProductTable tbody').empty();
        $('#OntologyStore-ExecuteBtn').attr('disabled', 'disabled');

        i2b2.OntologyStore.modal.progress.hide();
        i2b2.OntologyStore.modal.message.show(
                'Fail to Sync From Cloud',
                'Unable to retrieve a list of ontologies.');
    }, 500);
};

i2b2.OntologyStore.getSelectedProductIndexes = () => {
    let indexes = [];

    // get selected product download/install indexes
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

    return indexes;
};

i2b2.OntologyStore.getSelectedProducts = () => {
    let data = [];

    i2b2.OntologyStore.getSelectedProductIndexes()
            .forEach(index => {
                let product = i2b2.OntologyStore.products[index];
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

    return data;
};

i2b2.OntologyStore.execute = () => {
    let dat = [
        i2b2.OntologyStore.env.domain,
        i2b2.OntologyStore.env.project,
        i2b2.OntologyStore.env.user,
        i2b2.OntologyStore.env.session
    ];
    if (i2b2.OntologyStore.env.isAdmin) {
        let selectedProducts = i2b2.OntologyStore.getSelectedProducts();
        if (selectedProducts.length > 0) {
            i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');

            $.ajax({
                type: 'POST',
                headers: {
                    'X-I2B2-Domain': i2b2.OntologyStore.env.domain,
                    'X-I2B2-Project': i2b2.OntologyStore.env.project,
                    'Authorization': 'Basic ' + btoa(i2b2.OntologyStore.env.user + ':' + i2b2.OntologyStore.env.session)
                },
                url: 'http://' + location.host + '/ontology-store/action',
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(selectedProducts),
                success: function (data) {
                    let successHandler = (fetchedData) => {
                        setTimeout(function () {
                            i2b2.OntologyStore.products = JSON.parse(fetchedData);
                            i2b2.OntologyStore.refreshProductTable();

                            i2b2.OntologyStore.func.doRefreshAll();
                            i2b2.OntologyStore.modal.progress.hide();
                            i2b2.OntologyStore.modal.summary.show(data);
                        }, 500);
                    };
                    let errorHandler = () => {
                        i2b2.OntologyStore.func.doRefreshAll();
                        i2b2.OntologyStore.modal.progress.hide();
                        i2b2.OntologyStore.modal.summary.show(data);
                    };

                    i2b2.OntologyStore.syncFromCloudAction(successHandler, errorHandler);
                },
                error: function (data) {
                    let successHandler = (fetchedData) => {
                        setTimeout(function () {
                            i2b2.OntologyStore.products = JSON.parse(fetchedData);
                            i2b2.OntologyStore.refreshProductTable();

                            i2b2.OntologyStore.func.doRefreshAll();
                            i2b2.OntologyStore.modal.progress.hide();
                            i2b2.OntologyStore.modal.message.show(data.statusText, data.responseText);
                        }, 500);
                    };
                    let errorHandler = () => {
                        i2b2.OntologyStore.func.doRefreshAll();
                        i2b2.OntologyStore.modal.progress.hide();
                        i2b2.OntologyStore.modal.message.show(data.statusText, data.responseText);
                    };
                }
            });
        } else {
            // at least one ontology must be selected to download/install.
            i2b2.OntologyStore.modal.message.show('No Ontology Selected', 'Please select an ontology to download/install.');
        }
    } else {
        i2b2.OntologyStore.modal.message.show('Insufficient Privileges', 'Administrative privileges required!');
    }
};

window.addEventListener("I2B2_READY", () => {
    i2b2.authorizedTunnel.variable["i2b2.PM.model.isAdmin"].then((isAdmin) => {
        i2b2.OntologyStore.env.isAdmin = isAdmin;
    });
    i2b2.authorizedTunnel.function["i2b2.h.getDomain"]().then((domain) => {
        i2b2.OntologyStore.env.domain = domain;
    });
    i2b2.authorizedTunnel.function["i2b2.h.getProject"]().then((project) => {
        i2b2.OntologyStore.env.project = project;
    });
    i2b2.authorizedTunnel.function["i2b2.h.getUser"]().then((user) => {
        i2b2.OntologyStore.env.user = user;
    });
    i2b2.authorizedTunnel.function["i2b2.h.getPass"]().then((session) => {
        i2b2.OntologyStore.env.session = session;
    });
});