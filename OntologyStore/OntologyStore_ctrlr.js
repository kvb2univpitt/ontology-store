if (undefined === i2b2.OntologyStore.ontology) {
    i2b2.OntologyStore.ontology = {
        message: {
            createSummaryTable: function (data) {
                let table = document.createElement('table');
                table.id = 'action-summary';

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
                    columns[2].innerHTML = summary.inProgress ? '<span class="text-in-progress">In Progress</span>' : summary.success ? '<span class="text-success">Success</span>' : '<span class="text-fail">Failed</span>';
                    columns[3].innerHTML = summary.detail;

                    columns[2].className = "text-center";
                }

                return table;
            },
            panel: {},
            show: function (title, message) {
                document.getElementById("download-message-title").innerHTML = title;
                document.getElementById("download-message-body").innerHTML = message;

                if (!this.panel.error) {
                    let panel = new YAHOO.widget.Panel("download-message", {
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
                document.getElementById("download-message-title").innerHTML = "Download/Install Summary";
                document.getElementById("download-message-body").innerHTML = null;
                document.getElementById("download-message-body").appendChild(this.createSummaryTable(data));

                if (!this.panel.summary) {
                    let panel = new YAHOO.widget.Panel("download-message", {
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
        },
        modal: {
            show: function () {
                if (!i2b2.OntologyStore.ontology.modal.panel) {
                    let panel = new YAHOO.widget.Panel("download-modal", {
                        width: "200px",
                        fixedcenter: true,
                        close: false,
                        draggable: false,
                        zindex: 4,
                        modal: true,
                        visible: false
                    });
                    panel.render(document.body);
                    i2b2.OntologyStore.ontology.modal.panel = panel;
                }

                i2b2.OntologyStore.ontology.modal.panel.show();
            },
            hide: function () {
                if (i2b2.OntologyStore.ontology.modal.panel) {
                    i2b2.OntologyStore.ontology.modal.panel.hide();
                }
            }
        },
        products: [],
        table: {
            headers: [
                "Title",
                "Version",
                "Owner/Author",
                "Product Type",
                "Include Network Ontology Artifacts",
                "Included Terminologies",
                "Download",
                "Install",
                "Status"
            ],
            clear: function () {
                let table = document.getElementById("ontology-product-table");

                //create headers if none exists
                let tHead = table.tHead;
                if (!tHead) {
                    tHead = table.createTHead();
                    let row = tHead.insertRow(-1);
                    for (let i = 0; i < this.headers.length; i++) {
                        let th = document.createElement('th');
                        th.innerHTML = this.headers[i];
                        row.appendChild(th);
                    }
                }

                // remove existing data
                let tBodies = table.tBodies;
                if (tBodies.length > 0) {
                    for (let i = 0; i < tBodies.length; i++) {
                        table.removeChild(tBodies[i]);
                    }
                }
                table.createTBody();
            },
            addEmptyRow: function () {
                let table = document.getElementById("ontology-product-table");
                let tBody = (table.tBodies.length > 0) ? table.tBodies[0] : table.createTBody();

                // add empty row to show no data
                let row = tBody.insertRow(-1);
                for (let i = 0; i < this.headers.length; i++) {
                    row.insertCell(i);
                }
            },
            addRow: function (index, product) {
                let table = document.getElementById("ontology-product-table");
                let tBody = (table.tBodies.length > 0) ? table.tBodies[0] : table.createTBody();
                let row = tBody.insertRow(-1);

                // add columns
                let columns = [];
                for (let i = 0; i < this.headers.length; i++) {
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
                        columns[8].innerHTML = '<span class="text-success">Completed</span>';
                    } else if (product.failed) {
                        columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                        columns[8].innerHTML = '<span class="text-fail">Installation Failed</span>';
                    } else if (product.started) {
                        columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                        columns[8].innerHTML = '<span class="text-in-progress">Installation In Progress</span>';
                    } else {
                        columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
                        columns[8].innerHTML = 'Ready To Be Installed';
                    }
                } else if (product.failed) {
                    columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                    columns[8].innerHTML = '<span class="text-fail">Download Failed</span>';
                } else if (product.started) {
                    columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" disabled="disabled" />`;
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" disabled="disabled" />`;
                    columns[8].innerHTML = '<span class="text-in-progress">Download In Progress</span>';
                } else {
                    columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" />`;
                    columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;
                }

                // style columns
                columns[4].style = 'width: 150px; max-width: 150px';
                columns[4].className = "text-center";
                columns[6].style = 'width: 70px; max-width: 70px';
                columns[6].className = "text-center";
                columns[7].style = 'width: 50px; max-width: 50px';
                columns[7].className = "text-center";
                columns[8].style = 'width: 80px; max-width: 80px';
            }
        },
        button: {
            disable: function () {
                let executeBtn = document.getElementById("ontology-execute");
                if (executeBtn) {
                    executeBtn.disabled = true;
                }
            },
            enable: function () {
                let executeBtn = document.getElementById("ontology-execute");
                if (executeBtn) {
                    executeBtn.disabled = false;
                }
            },
            remove: function () {
                // remove execute buttton
                let executeBtn = document.getElementById("ontology-execute");
                if (executeBtn) {
                    executeBtn.remove();
                }
            },
            add: function () {
                // add button to perform actions on selected rows in the table
                let executeBtn = document.getElementById("ontology-execute");
                if (!executeBtn) {
                    executeBtn = document.createElement("button");
                    executeBtn.id = "ontology-execute";
                    executeBtn.type = "button";
                    executeBtn.innerHTML = "Execute";
                    executeBtn.onclick = this.action;
                    document.getElementById("ontology-products")
                            .appendChild(executeBtn);
                }
            },
            action: function () {
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
                    let product = i2b2.OntologyStore.ontology.products[index];
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
                        i2b2.OntologyStore.ontology.button.disable();
                        i2b2.OntologyStore.ontology.modal.show();
                        jQuery.ajax({
                            type: 'POST',
                            headers: {
                                'X-I2B2-User': i2b2.h.getUser(),
                                'X-I2B2-Pass': i2b2.h.getPass(),
                                'X-I2B2-Domain': i2b2.h.getDomain(),
                                'X-I2B2-Project': i2b2.h.getProject()
                            },
                            url: 'http://' + location.host + '/ontology-store/action',
                            contentType: "application/json; charset=utf-8",
                            data: JSON.stringify(data)
                        }).fail(function (data) {
                            i2b2.OntologyStore.ontology.message.show(data.statusText, data.responseText);
                        }).done(function (data) {
                            i2b2.OntologyStore.ontology.message.showSummary(data);
                            jQuery('#ontology-download').click();
                        }).always(function () {
                            i2b2.OntologyStore.ontology.button.enable();
                            i2b2.OntologyStore.ontology.modal.hide();
                        });
                    } else {
                        i2b2.OntologyStore.ontology.message.show('Insufficient Privileges', 'Administrative privileges required!');
                    }
                } else {
                    i2b2.OntologyStore.ontology.message.show('No Ontology Selected', 'Please select an ontology to download/install.');
                }
            }
        }
    };
}

i2b2.OntologyStore.Init = function (loadedDiv) {
    i2b2.OntologyStore.view.containerDiv = loadedDiv;
    jQuery('#ontology-download').click(function () {
        jQuery.ajax({
            type: 'GET',
            dataType: 'text',
            url: 'http://' + location.host + '/ontology-store/products',
        }).done(function (data) {
            let ontology = i2b2.OntologyStore.ontology;
            let table = ontology.table;
            let button = ontology.button;

            table.clear();

            ontology.products = JSON.parse(data);
            if (ontology.products.length > 0) {
                jQuery.each(ontology.products, function (index, product) {
                    table.addRow(index, product);
                });

                button.add();
            } else {
                table.addEmptyRow();
                button.remove();
            }
        }).fail(function () {
            console.log('fail');
        }).always(function () {
            console.log('always');
        });
    });
};

i2b2.OntologyStore.Unload = function () {
    return true;
};
