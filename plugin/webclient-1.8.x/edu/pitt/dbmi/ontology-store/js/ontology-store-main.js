i2b2.OntologyStore = {};

// ontologies fetched from cloud
i2b2.OntologyStore.products = [];

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
            $('#OntologyStore-MessageModalMessage').html(message);
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

i2b2.OntologyStore.syncFromCloud = {};
i2b2.OntologyStore.syncFromCloud.action = (successHandler, errorHandler) => {
    i2b2.ajax.ONTSTORE.GetProducts().then(successHandler).catch(errorHandler);
};
i2b2.OntologyStore.syncFromCloud.parseResults = (resultXmlStr) => {
    let models = [];
    const parser = new DOMParser();
    const doc = parser.parseFromString(resultXmlStr, 'text/xml');
    let products = doc.getElementsByTagName('product');
    for (let i = 0; i < products.length; i++) {
        let product = products[i];

        let obj = new Object;
        obj.id = product.getElementsByTagName('id')[0].childNodes[0].nodeValue;
        obj.title = product.getElementsByTagName('title')[0].childNodes[0].nodeValue;
        obj.version = product.getElementsByTagName('version')[0].childNodes[0].nodeValue;
        obj.owner = product.getElementsByTagName('owner')[0].childNodes[0].nodeValue;
        obj.type = product.getElementsByTagName('type')[0].childNodes[0].nodeValue;
        obj.terminologies = [];
        obj.includeNetworkPackage = 'true' === product.getElementsByTagName('include_network_package')[0].childNodes[0].nodeValue;
        obj.downloaded = 'true' === product.getElementsByTagName('downloaded')[0].childNodes[0].nodeValue;
        obj.installed = 'true' === product.getElementsByTagName('installed')[0].childNodes[0].nodeValue;
        obj.started = 'true' === product.getElementsByTagName('started')[0].childNodes[0].nodeValue;
        obj.failed = 'true' === product.getElementsByTagName('failed')[0].childNodes[0].nodeValue;
        obj.disabled = 'true' === product.getElementsByTagName('disabled')[0].childNodes[0].nodeValue;

        const statusDetail = product.getElementsByTagName('status_detail');
        obj.statusDetail = (statusDetail.length > 0) ? statusDetail[0].childNodes[0].nodeValue : '';

        // populate terminologies
        const terminologies = product.getElementsByTagName('terminology');
        for (let j = 0; j < terminologies.length; j++) {
            obj.terminologies.push(terminologies[j].textContent);
        }

        models.push(obj);
    }

    return models;
};
i2b2.OntologyStore.syncFromCloud.successHandler = (resultXmlStr) => {
    setTimeout(() => {
        i2b2.OntologyStore.products = i2b2.OntologyStore.syncFromCloud.parseResults(resultXmlStr);

        i2b2.OntologyStore.table.refresh();
        $('#OntologyStore-ExecuteBtn').removeAttr('disabled');
        i2b2.OntologyStore.modal.progress.hide();
    }, 500);
};
i2b2.OntologyStore.syncFromCloud.errorHandler = () => {
    setTimeout(() => {
        i2b2.OntologyStore.modal.progress.hide();
    }, 500);
};
i2b2.OntologyStore.syncFromCloud.clickAction = () => {
    i2b2.OntologyStore.modal.progress.show('Sync From Clould');
    i2b2.OntologyStore.syncFromCloud.action(
            i2b2.OntologyStore.syncFromCloud.successHandler,
            i2b2.OntologyStore.syncFromCloud.errorHandler);
};

i2b2.OntologyStore.execute = {};
i2b2.OntologyStore.execute.action = (selectedProducts, successHandler, errorHandler) => {
    let options = {
        products_str_xml: i2b2.OntologyStore.productsToXml(selectedProducts)
    };
    i2b2.ajax.ONTSTORE.PerformProductActions(options)
            .then(successHandler)
            .catch(errorHandler);
};
i2b2.OntologyStore.execute.parseResults = (resultXmlStr) => {
    let models = [];
    const parser = new DOMParser();
    const doc = parser.parseFromString(resultXmlStr, 'text/xml');
    let actionSummaries = doc.getElementsByTagName('action_summary');
    for (let i = 0; i < actionSummaries.length; i++) {
        let actionSummary = actionSummaries[i];

        let obj = new Object;
        obj.title = actionSummary.getElementsByTagName('title')[0].childNodes[0].nodeValue;
        obj.actionType = actionSummary.getElementsByTagName('action_type')[0].childNodes[0].nodeValue;
        obj.inProgress = actionSummary.getElementsByTagName('in_progress')[0].childNodes[0].nodeValue;
        obj.success = actionSummary.getElementsByTagName('success')[0].childNodes[0].nodeValue;
        obj.detail = actionSummary.getElementsByTagName('detail')[0].childNodes[0].nodeValue;

        models.push(obj);
    }

    return models;
};
i2b2.OntologyStore.execute.successHandler = (resultXmlStr) => {
    i2b2.ajax.ONTSTORE.GetProducts().then((resultXmlStr) => {
        i2b2.OntologyStore.products = i2b2.OntologyStore.syncFromCloud.parseResults(resultXmlStr);
        i2b2.OntologyStore.table.refresh();
    });

    setTimeout(() => {
        let data = i2b2.OntologyStore.execute.parseResults(resultXmlStr);
        for (let i = 0; i < data.length; i++) {
            if ((data[i].actionType === 'Install') || (data[i].actionType === 'Enable') || (data[i].actionType === 'Disable')) {
                i2b2.authorizedTunnel.function["i2b2.ONT.view.nav.doRefreshAll"]();
                break;
            }
        }

        $('#OntologyStore-ExecuteBtn').prop("disabled", false);
        i2b2.OntologyStore.modal.progress.hide();
        i2b2.OntologyStore.modal.summary.show(data);
    }, 500);
};
i2b2.OntologyStore.execute.errorHandler = (resultsXmlStr) => {
    setTimeout(() => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(resultsXmlStr, 'text/xml');

        let msgTitle = '';
        let msgBody = '';
        let status = doc.getElementsByTagName('status');
        if (status.length === 0) {
            msgTitle = 'Server Error';
            msgBody = '<p class="text-danger fw-bold">Internal server error!</p>';
        }

        $('#OntologyStore-ExecuteBtn').prop("disabled", false);
        i2b2.OntologyStore.modal.progress.hide();
        i2b2.OntologyStore.modal.message.show(msgTitle, msgBody);
    }, 500);
};
i2b2.OntologyStore.execute.clickAction = () => {
    let products = i2b2.OntologyStore.products;
    if (products && products.length > 0) {
        i2b2.authorizedTunnel.variable["i2b2.PM.model.isAdmin"].then((isAdmin) => {
            if (isAdmin) {
                let selectedProducts = i2b2.OntologyStore.getSelectedProducts(products);
                if (selectedProducts.length > 0) {
                    $('#OntologyStore-ExecuteBtn').prop("disabled", true);
                    i2b2.OntologyStore.modal.progress.show('Download/Install Ontology');
                    i2b2.OntologyStore.execute.action(
                            selectedProducts,
                            i2b2.OntologyStore.execute.successHandler,
                            i2b2.OntologyStore.execute.errorHandler);
                } else {
                    // at least one ontology must be selected to download/install.
                    i2b2.OntologyStore.modal.message.show('No Ontology Selected', 'Please select an ontology to download/install.');
                }
            } else {
                // requires administrative privileges
                i2b2.OntologyStore.modal.message.show('Insufficient Privileges', '<p class="text-danger fw-bold">Administrative privileges required!</p>');
            }
        });
    }
};

i2b2.OntologyStore.productToXml = (product) => {
    let tags = [];

    tags.push('            <product_action>');
    tags.push('                <id>' + product.id + '</id>');
    tags.push('                <include_network_package>' + product.includeNetworkPackage + '</include_network_package>');
    tags.push('                <download>' + product.download + '</download>');
    tags.push('                <install>' + product.install + '</install>');
    tags.push('                <disable_enable>' + product.disableEnable + '</disable_enable>');
    tags.push('            </product_action>');

    return tags.join('\n');
};
i2b2.OntologyStore.productsToXml = (products) => {
    let xml = [];
    for (let i = 0; i < products.length; i++) {
        xml.push(i2b2.OntologyStore.productToXml(products[i]));
    }

    return xml.join('\n');
};
i2b2.OntologyStore.getSelectedProductIndexes = (products) => {
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

i2b2.OntologyStore.getSelectedProducts = (products) => {
    let data = [];

    let selectedProductIndexes = i2b2.OntologyStore.getSelectedProductIndexes(products);
    for (let i = 0; i < selectedProductIndexes.length; i++) {
        let productIndex = selectedProductIndexes[i];
        let product = products[productIndex];
        let includeNetChkbx = document.getElementById(`network-${productIndex}`);
        let downloadChkbx = document.getElementById(`download-${productIndex}`);
        let installChkbx = document.getElementById(`install-${productIndex}`);
        let disableChkbx = document.getElementById(`disable-${productIndex}`);

        data.push({
            id: product.id,
            includeNetworkPackage: includeNetChkbx.checked,
            download: downloadChkbx.disabled ? false : downloadChkbx.checked,
            install: installChkbx.disabled ? false : installChkbx.checked,
            disableEnable: !(product.disabled === disableChkbx.checked)
        });
    }

    return data;
};

i2b2.OntologyStore.checkbox = {};
i2b2.OntologyStore.checkbox.downloadAction = (productIndex) => {
    let installChkbx = document.getElementById(`install-${productIndex}`);
    let downloadChkbx = document.getElementById(`download-${productIndex}`);
    let disableChkbx = document.getElementById(`disable-${productIndex}`);

    if (!downloadChkbx.checked) {
        // unselect install when download is not selected
        installChkbx.checked = false;

        // unselect disable when download is not selected
        disableChkbx.checked = false;

        // prevent selection when download is not selected
        disableChkbx.disabled = !installChkbx.checked;
    }
};
i2b2.OntologyStore.checkbox.installAction = (productIndex) => {
    let installChkbx = document.getElementById(`install-${productIndex}`);
    let downloadChkbx = document.getElementById(`download-${productIndex}`);
    let disableChkbx = document.getElementById(`disable-${productIndex}`);

    if (installChkbx.checked) {
        // automatically select download if install is selected
        downloadChkbx.checked = true;
    } else {
        // unselect disable when install is not selected
        disableChkbx.checked = false;
    }

    // prevent selection when install is not selected
    disableChkbx.disabled = !installChkbx.checked;
};

i2b2.OntologyStore.showFailedDownloadStatusDetails = (index) => {
    let product = i2b2.OntologyStore.products[index];
    if (product) {
        let msg = '<p class="text-danger fw-bold">' + product.statusDetail + '</p>';
        msg += '<p>To reinstall, please fix the issue and then delete the file <b>install.failed</b> from the folder <b><i>' + product.id + '</i></b> in the download directory on the server.</p>';
        msg += '<p>Please search for "OntologyInstallService" in the Wildfly\'s server log (<b>server.log</b>) for more detail.</p>';
        i2b2.OntologyStore.modal.message.show('Status Detail', msg);
    }
};

i2b2.OntologyStore.table = {};
i2b2.OntologyStore.table.refresh = () => {
    let datatables = i2b2.OntologyStore.table.datatables;
    datatables.clear();
    i2b2.OntologyStore.products.forEach((product, index, array) => {
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
        columns[9] = '<input type="checkbox" class="form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" disabled="disabled" />';

        if (product.includeNetworkPackage) {
            if (product.downloaded) {
                columns[4] = '<input type="checkbox" class="form-check-input" id="network-' + index + '" name="network" checked="checked" disabled="disabled" />';
            } else {
                columns[4] = '<input type="checkbox" class="form-check-input" id="network-' + index + '" name="network" checked="checked" />';
            }
        } else {
            columns[4] = '<input type="checkbox" class="form-check-input" id="network-' + index + '" name="network" disabled="disabled" />';
        }

        columns[5] = product.terminologies.join(',');

        if (product.downloaded) {
            columns[6] = '<input type="checkbox" class="form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';

            if (product.installed) {
                columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="text-success fw-bold">Installed</span>';

                if (product.disabled) {
                    columns[9] = '<input type="checkbox" class="form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" checked="checked" />';
                } else {
                    columns[9] = '<input type="checkbox" class="form-check-input" id="disable-' + index + '" data-id="' + index + '" name="disable" />';
                }
            } else if (product.failed) {
                columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<a href="#" class="text-decoration-none" onclick="i2b2.OntologyStore.showFailedInstallStatusDetails(' + index + '); return false;"><span class="text-danger fw-bold">Installation Failed</span></a>';
            } else if (product.started) {
                columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" checked="checked" disabled="disabled" />';
                columns[8] = '<span class="text-info fw-bold">Installation In Progress</span>';
            } else {
                columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" onclick="i2b2.OntologyStore.checkbox.installAction(' + index + ')" />';
                columns[8] = '<span class="text-warning fw-bold">Ready To Be Installed</span>';
            }
        } else if (product.failed) {
            columns[6] = '<input type="checkbox" class="form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" disabled="disabled" />';
            columns[8] = '<a href="#" class="text-decoration-none" onclick="i2b2.OntologyStore.showFailedDownloadStatusDetails(' + index + '); return false;"><span class="text-danger fw-bold">Download Failed</span></a>';
        } else if (product.started) {
            columns[6] = '<input type="checkbox" class="form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" checked="checked" disabled="disabled" />';
            columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" disabled="disabled" />';
            columns[8] = '<span class="text-info fw-bold">Download In Progress</span>';
        } else {
            columns[6] = '<input type="checkbox" class="form-check-input" id="download-' + index + '" data-id="' + index + '" name="download" onclick="i2b2.OntologyStore.checkbox.downloadAction(' + index + ')" />';
            columns[7] = '<input type="checkbox" class="form-check-input" id="install-' + index + '" data-id="' + index + '" name="install" onclick="i2b2.OntologyStore.checkbox.installAction(' + index + ')" />';
        }

        datatables.row.add(columns);
    });
    datatables.draw();
};

// ---------------------------------------------------------------------------------------
window.addEventListener("I2B2_READY", () => {
    i2b2.OntologyStore.table.datatables = $('#OntologyStore-ProductTable').DataTable({
        columnDefs: [
            {targets: 0, className: 'ontstore-title', width: '40%'},
            {targets: 4, className: 'text-center ontstore-network-chkbx', width: '75px', orderable: false},
            {targets: 6, className: 'text-center', orderable: false},
            {targets: 7, className: 'text-center', orderable: false},
            {targets: 9, className: 'text-center', orderable: false}
        ]
    });

    $('#OntologyStore-SyncFromCloud').on('click', i2b2.OntologyStore.syncFromCloud.clickAction);
    $('#OntologyStore-ExecuteBtn').on('click', i2b2.OntologyStore.execute.clickAction);

    // fetch ontologies from cloud
    i2b2.OntologyStore.syncFromCloud.clickAction();
});