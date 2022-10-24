<?php

require './xml_requests.php';

function performProductActions() {
    $uri = 'http://localhost:9090/i2b2/services/OntologyStoreService/getProductActions';
    $request = getProductionActionRequest($uri);

    $ch = curl_init($uri);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: text/xml'));
    curl_setopt($ch, CURLOPT_POSTFIELDS, "$request");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);

    return curl_exec($ch);
}

function getProducts() {
    $uri = 'http://localhost:9090/i2b2/services/OntologyStoreService/getProducts';
    $request = getProductsRequest($uri);

    $ch = curl_init($uri);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: text/xml'));
    curl_setopt($ch, CURLOPT_POSTFIELDS, "$request");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);

    return curl_exec($ch);
}

function getChildren($parent, $num_children, $max_results) {
    $uri = 'http://localhost:9090/i2b2/services/OntologyService/getChildren';
    $request = getChildrenRequest($uri, $parent, $num_children, $max_results);

    $ch = curl_init($uri);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: text/xml'));
    curl_setopt($ch, CURLOPT_POSTFIELDS, "$request");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);

    return curl_exec($ch);
}
