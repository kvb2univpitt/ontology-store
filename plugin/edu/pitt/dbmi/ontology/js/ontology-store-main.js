i2b2.PM = {
    model: {
        isAdmin: true
    }
};
i2b2.h.getProject = function () {
    return 'Demo';
};
i2b2.h.getDomain = function () {
    return 'i2b2demo';
};

i2b2.OntologyStore = {
    products: [],
    table: {
        numOfHeaders: 9,
        clear: function () {
            $('table#productTable tbody').empty();
        },
        show: function () {
            $('#ontologyList').show();
        },
        addRow: function (index, product) {
            let table = document.getElementById('productTable');
            let tBody = (table.tBodies.length > 0) ? table.tBodies[0] : table.createTBody();
            let row = tBody.insertRow(-1);

            // add columns
            let columns = [];
            for (let i = 0; i < this.numOfHeaders; i++) {
                columns[i] = row.insertCell(i);
            }

            // add column data
            columns[0].innerHTML = product.title;
            columns[1].innerHTML = product.version;
            columns[2].innerHTML = product.owner;
            columns[3].innerHTML = product.type;
            if (product.downloaded) {
                columns[4].innerHTML = product.includeNetworkPackage
                        ? `<input id="network-${index}" type="checkbox" name="network" checked="checked" disabled="disabled" />`
                        : `<input id="network-${index}" type="checkbox" name="network" disabled="disabled" />`;
            } else {
                columns[4].innerHTML = product.includeNetworkPackage
                        ? `<input id="network-${index}" type="checkbox" name="network" checked="checked" />`
                        : `<input id="network-${index}" type="checkbox" name="network" />`;
            }
            columns[5].innerHTML = product.terminologies.join(',');

            if (product.downloaded) {
                columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;

                if (product.installed) {
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                    columns[8].innerHTML = '<span class="text-success fw-bold">Completed</span>';
                } else if (product.failed) {
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                    columns[8].innerHTML = '<span class="text-danger fw-bold">Installation Failed</span>';
                } else if (product.started) {
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                    columns[8].innerHTML = '<span class="text-primary fw-bold">Installation In Progress</span>';
                } else {
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
                    columns[8].innerHTML = 'Ready To Be Installed';
                }
            } else if (product.failed) {
                columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
                columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8].innerHTML = '<span class="text-danger fw-bold">Download Failed</span>';
            } else if (product.started) {
                columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
                columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                columns[8].innerHTML = '<span class="text-primary fw-bold">Download In Progress</span>';
            } else {
                columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" />`;
                columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
            }

            // align text
            columns[4].className = "text-center";
            columns[6].className = "text-center";
            columns[7].className = "text-center";

            // style columns
            columns[4].style = 'width: 150px; max-width: 150px';
            columns[6].style = 'width: 70px; max-width: 70px';
            columns[7].style = 'width: 50px; max-width: 50px';
            columns[8].style = 'width: 90px; max-width: 90px';
        }
    }
};

i2b2.OntologyStore.syncFromCloud = function () {
    $.ajax({
        type: 'GET',
        dataType: 'text',
        url: 'http://' + location.host + '/ontology-store/products',
        success: function (data) {
            let ontStore = i2b2.OntologyStore;
            ontStore.products = JSON.parse(data);

            let table = ontStore.table;
            table.clear();

            if (ontStore.products.length > 0) {
                $.each(ontStore.products, function (index, product) {
                    table.addRow(index, product);
                });
                $('#executeBtn').removeAttr('disabled');
            } else {
                $('#executeBtn').attr('disabled', 'disabled');
            }

            table.show();
        },
        error: function (e) {
            $('#messageModalLabel').text('Fail to Sync From Cloud');
            $('#messageModalMessage').text('Unable to retrieve a list of ontologies.');
            $('#messageModal').modal('show');
        },
        complete: function (e) {
            $('#progressModal').modal('hide');
        }
    });
};

i2b2.OntologyStore.showSummary = function (data) {
    $('table#summaryTable tbody').empty();

    let numOfHeaders = 4;
    let table = document.getElementById('summaryTable');
    let tBody = (table.tBodies.length > 0) ? table.tBodies[0] : table.createTBody();
    for (let i = 0; i < data.length; i++) {
        // create row with columns
        let columns = [];
        let row = tBody.insertRow(-1);
        for (let i = 0; i < numOfHeaders; i++) {
            columns[i] = row.insertCell(i);
        }

        let summary = data[i];
        columns[0].innerHTML = summary.title;
        columns[1].innerHTML = summary.actionType;
        columns[2].innerHTML = summary.inProgress ?
                '<span class="text-in-progress">In Progress</span>' : summary.success ? '<span class="text-success fw-bold">Success</span>'
                : '<span class="text-danger fw-bold">Failed</span>';
        columns[3].innerHTML = summary.detail;

        columns[2].className = "text-center";
    }

    $('#summaryModal').modal('show');
};

i2b2.OntologyStore.execute = function () {
    if (i2b2.PM.model.isAdmin) {
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

        if (indexes.length > 0) {
            let data = [];
            indexes.forEach(index => {
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

            $.ajax({
                type: 'POST',
                headers: {
                    'X-I2B2-Domain': i2b2.h.getDomain(),
                    'X-I2B2-Project': i2b2.h.getProject()
                },
                url: 'http://' + location.host + '/ontology-store/action',
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(data),
                success: function (data) {
                    i2b2.OntologyStore.showSummary(data);
                },
                error: function (data) {
                    $('#messageModalLabel').text(data.statusText);
                    $('#messageModalMessage').text(data.responseText);
                    $('#messageModal').modal('show');
                },
                complete: function (e) {
                    i2b2.OntologyStore.syncFromCloud();
                    $('#progressModal').modal('hide');
                }
            });
        } else {
            $('#progressModal').modal('hide');

            $('#messageModalLabel').text('No Ontology Selected');
            $('#messageModalMessage').text('Please select an ontology to download/install.');
            $('#messageModal').modal('show');
        }
    } else {
        $('#progressModal').modal('hide');

        $('#messageModalLabel').text('Insufficient Privileges');
        $('#messageModalMessage').text('Administrative privileges required!');
        $('#messageModal').modal('show');
    }
};

$(document).ready(function () {
    $('#syncFromCloud').click(function (evt) {
        $('#progressModalLabel').text('Fetching Ontologies From Cloud');
        setTimeout(function () {
            i2b2.OntologyStore.syncFromCloud();
        }, 500);
    });


    $('#executeBtn').click(function () {
        $('#progressModalLabel').text('Downloading/Installing Ontologies');
        setTimeout(function () {
            i2b2.OntologyStore.execute();
        }, 500);
    });
});
