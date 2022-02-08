if (undefined === i2b2.OntologyStore.ontology) {
    i2b2.OntologyStore.ontology = {
        message: {
            show: function (data) {
                document.getElementById("download-message-title").innerHTML = data.statusText;
                document.getElementById("download-message-body").innerHTML = data.responseText;
                if (!i2b2.OntologyStore.ontology.message.panel) {
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
                    i2b2.OntologyStore.ontology.message.panel = panel;
                }

                i2b2.OntologyStore.ontology.message.panel.show();
            },
            hide: function () {
                if (i2b2.OntologyStore.ontology.message.panel) {
                    i2b2.OntologyStore.ontology.message.panel.hide();
                }
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
                "Install"
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
                columns[4].innerHTML = product.includeNetworkPackage
                        ? `<input id="network-${index}" type="checkbox" name="network" checked="checked" />`
                        : `<input id="network-${index}" type="checkbox" name="network" />`;
                columns[5].innerHTML = product.terminologies.join(',');
                columns[6].innerHTML = `<input id="download-${index}" data-id="${index}" type="checkbox" name="download" />`;
                columns[7].innerHTML = `<input id="install-${index}" data-id="${index}" type="checkbox" name="install" />`;

                // style columns
                columns[4].style = 'width: 150px; max-width: 150px';
                columns[4].className = "text-center";
                columns[6].style = 'width: 70px; max-width: 70px';
                columns[6].className = "text-center";
                columns[7].style = 'width: 50px; max-width: 50px';
                columns[7].className = "text-center";
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
                    i2b2.OntologyStore.ontology.button.disable();
                    i2b2.OntologyStore.ontology.modal.show();
                    jQuery.ajax({
                        type: 'POST',
                        url: 'http://' + location.host + '/ontology-store/action',
                        contentType: "application/json; charset=utf-8",
                        data: JSON.stringify(data)
                    }).fail(function (data) {
                        i2b2.OntologyStore.ontology.message.show(data);
                    }).always(function () {
                        i2b2.OntologyStore.ontology.button.enable();
                        i2b2.OntologyStore.ontology.modal.hide();
                    });
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
