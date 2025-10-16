// create the communicator Object
i2b2.ONTSTORE.ajax = i2b2.hive.communicatorFactory("ONTSTORE");
i2b2.ONTSTORE.cfg.parsers = {};
i2b2.ONTSTORE.cfg.parsers.ExtractProductResults = function () {
    this.model = [];

    if (this.error) {
        console.error("[ExtractProducts] Could not parse() data!");
    } else {
        let products = this.refXML.getElementsByTagName('product');
        for (let i = 0; i < products.length; i++) {
            let product = products[i];

            let obj = new Object;
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
            let terminologies = i2b2.h.XPath(product, "descendant-or-self::terminology/node()");
            for (let j = 0; j < terminologies.length; j++) {
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
i2b2.ONTSTORE.cfg.msgs.GetProducts = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        {{{proxy_info}}}
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>i2b2 OntologyStore</application_name>
            <application_version>${i2b2.ClientVersion}</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>${i2b2.ClientVersion}</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>
        <security>
            <domain>{{{sec_domain}}}</domain>
            <username>{{{sec_user}}}</username>
            {{{sec_pass_node}}}
        </security>
        <message_control_id>
            <message_num>{{{header_msg_id}}}</message_num>
            <instance_num>0</instance_num>
        </message_control_id>
        <processing_id>
            <processing_id>P</processing_id>
            <processing_mode>I</processing_mode>
        </processing_id>
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>
        <application_acknowledgement_type>AL</application_acknowledgement_type>
        <country_code>US</country_code>
        <project_id>{{{sec_project}}}</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:getProducts/>
    </message_body>
</ns3:request>`;
i2b2.ONTSTORE.ajax._addFunctionCall("GetProducts", "{{{URL}}}getProducts", i2b2.ONTSTORE.cfg.msgs.GetProducts, null, i2b2.ONTSTORE.cfg.parsers.ExtractProductResults);

i2b2.ONTSTORE.cfg.msgs.PerformProductActions = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/"
             xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/"
             xmlns:ns4="http://www.i2b2.org/xsd/cell/ontologystore/1.1/">
    <message_header>
        {{{proxy_info}}}
        <i2b2_version_compatible>1.1</i2b2_version_compatible>
        <hl7_version_compatible>2.4</hl7_version_compatible>
        <sending_application>
            <application_name>i2b2 OntologyStore</application_name>
            <application_version>${i2b2.ClientVersion}</application_version>
        </sending_application>
        <sending_facility>
            <facility_name>i2b2 Hive</facility_name>
        </sending_facility>
        <receiving_application>
            <application_name>OntologyStore Cell</application_name>
            <application_version>${i2b2.ClientVersion}</application_version>
        </receiving_application>
        <receiving_facility>
            <facility_name>i2b2 Hive</facility_name>
        </receiving_facility>
        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>
        <security>
            <domain>{{{sec_domain}}}</domain>
            <username>{{{sec_user}}}</username>
            {{{sec_pass_node}}}
        </security>
        <message_control_id>
            <message_num>{{{header_msg_id}}}</message_num>
            <instance_num>0</instance_num>
        </message_control_id>
        <processing_id>
            <processing_id>P</processing_id>
            <processing_mode>I</processing_mode>
        </processing_id>
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>
        <application_acknowledgement_type>AL</application_acknowledgement_type>
        <country_code>US</country_code>
        <project_id>{{{sec_project}}}</project_id>
    </message_header>
    <request_header>
        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>
    </request_header>
    <message_body>
        <ns4:product_actions>
            {{{products_str_xml}}}
        </ns4:product_actions>
    </message_body>
</ns3:request>`;
i2b2.ONTSTORE.ajax._addFunctionCall("PerformProductActions", "{{{URL}}}getProductActions", i2b2.ONTSTORE.cfg.msgs.PerformProductActions, ['products_str_xml'], i2b2.ONTSTORE.cfg.parsers.ExtractProductActionResults);
