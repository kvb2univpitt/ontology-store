// create the communicator Object
i2b2.ONTSTORE.ajax = i2b2.hive.communicatorFactory("ONTSTORE");
i2b2.ONTSTORE.cfg.parsers = {};
i2b2.ONTSTORE.cfg.parsers.ExtractProductResults = function () {
    this.model = [];

    if (this.error) {
        console.error("[ExtractProducts] Could not parse() data!");
    } else {
        var products = this.refXML.getElementsByTagName('product');
        for (var i = 0; i < products.length; i++) {
            var product = products[i];

            var obj = new Object;
            obj.id = i2b2.h.getXNodeVal(product, 'id');
            obj.title = i2b2.h.getXNodeVal(product, 'title');
            obj.version = i2b2.h.getXNodeVal(product, 'version');
            obj.owner = i2b2.h.getXNodeVal(product, 'owner');
            obj.type = i2b2.h.getXNodeVal(product, 'type');
            obj.terminologies = [];
            obj.includeNetworkPackage = 'true' === i2b2.h.getXNodeVal(product, 'include_network_package');
            obj.downloaded = 'true' === i2b2.h.getXNodeVal(product, 'downloaded');
            obj.installed = 'true' === i2b2.h.getXNodeVal(product, 'installed');
            obj.started = 'true' === i2b2.h.getXNodeVal(product, 'started');
            obj.failed = 'true' === i2b2.h.getXNodeVal(product, 'failed');
            obj.disabled = 'true' === i2b2.h.getXNodeVal(product, 'disabled');
            obj.statusDetail = i2b2.h.getXNodeVal(product, 'status_detail');

            // populate terminologies
            var terminologies = i2b2.h.XPath(product, "descendant-or-self::terminology/node()");
            for (var j = 0; j < terminologies.length; j++) {
                obj.terminologies.push(terminologies[j].nodeValue);
            }

            this.model.push(obj);
        }
    }

    return this.model;
};

i2b2.ONTSTORE.cfg.parsers.ExtractProductActionResults = function () {
    this.model = [];

    if (this.error) {
        console.error("[ExtractProducts] Could not parse() data!");
    } else {
        var actionSummaries = this.refXML.getElementsByTagName('action_summary');
        for (var i = 0; i < actionSummaries.length; i++) {
            var actionSummary = actionSummaries[i];

            var obj = new Object;
            obj.title = i2b2.h.getXNodeVal(actionSummary, 'title');
            obj.actionType = i2b2.h.getXNodeVal(actionSummary, 'action_type');
            obj.inProgress = 'true' === i2b2.h.getXNodeVal(actionSummary, 'in_progress');
            obj.success = 'true' === i2b2.h.getXNodeVal(actionSummary, 'success');
            obj.detail = i2b2.h.getXNodeVal(actionSummary, 'detail');

            this.model.push(obj);
        }
    }

    return this.model;
};

i2b2.ONTSTORE.cfg.msgs = {};
i2b2.ONTSTORE.cfg.msgs.GetProducts = '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"\n' +
        '             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">\n' +
        '    <message_header>\n' +
        '        {{{proxy_info}}}' +
        '        <sending_application>\n' +
        '            <application_name>i2b2 OntologyStore</application_name>\n' +
        '            <application_version>{{{version}}}</application_version>\n' +
        '        </sending_application>\n' +
        '        <sending_facility>\n' +
        '            <facility_name>i2b2 Hive</facility_name>\n' +
        '        </sending_facility>\n' +
        '        <security>\n' +
        '            <domain>{{{sec_domain}}}</domain>\n' +
        '            <username>{{{sec_user}}}</username>\n' +
        '            {{{sec_pass_node}}}\n' +
        '        </security>\n' +
        '        <project_id>{{{sec_project}}}</project_id>\n' +
        '    </message_header>\n' +
        '    <request_header>\n' +
        '        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n' +
        '    </request_header>\n' +
        '    <message_body>\n' +
        '        <ns4:getProducts></ns4:getProducts>\n' +
        '    </message_body>\n' +
        '</ns3:request>';
i2b2.ONTSTORE.ajax._addFunctionCall("GetProducts", "{{{URL}}}getProducts", i2b2.ONTSTORE.cfg.msgs.GetProducts, null, i2b2.ONTSTORE.cfg.parsers.ExtractProductResults);

i2b2.ONTSTORE.cfg.msgs.PerformProductActions = '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"\n' +
        '             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">\n' +
        '    <message_header>\n' +
        '        {{{proxy_info}}}' +
        '        <sending_application>\n' +
        '            <application_name>i2b2 OntologyStore</application_name>\n' +
        '            <application_version>{{{version}}}</application_version>\n' +
        '        </sending_application>\n' +
        '        <sending_facility>\n' +
        '            <facility_name>i2b2 Hive</facility_name>\n' +
        '        </sending_facility>\n' +
        '        <security>\n' +
        '            <domain>{{{sec_domain}}}</domain>\n' +
        '            <username>{{{sec_user}}}</username>\n' +
        '            {{{sec_pass_node}}}\n' +
        '        </security>\n' +
        '        <project_id>{{{sec_project}}}</project_id>\n' +
        '    </message_header>\n' +
        '    <request_header>\n' +
        '        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n' +
        '    </request_header>\n' +
        '    <message_body>\n' +
        '        <ns4:product_actions>\n' +
        '{{{products_str_xml}}}\n' +
        '        </ns4:product_actions>\n' +
        '    </message_body>\n' +
        '</ns3:request>';
i2b2.ONTSTORE.ajax._addFunctionCall("PerformProductActions", "{{{URL}}}getProductActions", i2b2.ONTSTORE.cfg.msgs.PerformProductActions, ['products_str_xml'], i2b2.ONTSTORE.cfg.parsers.ExtractProductActionResults);
