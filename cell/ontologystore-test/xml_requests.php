<?php

$domain = 'i2b2demo';
$username = 'i2b2';
$password = 'demouser';

function getProductionActionRequest($uri) {
    $requestXml = file_get_contents('./request/product_actions.xml', FILE_USE_INCLUDE_PATH);
    $requestXml = str_replace("I2B2_URI", $uri, $requestXml);

    return $requestXml;
}

function getProductsRequest($uri) {
    $requestXml = file_get_contents('./request/get_products.xml', FILE_USE_INCLUDE_PATH);
    $requestXml = str_replace("I2B2_URI", $uri, $requestXml);

    return $requestXml;
}

function getChildrenRequest($uri, $parent, $num_children, $max_results) {
    $requestXml = file_get_contents('./request/get_children.xml', FILE_USE_INCLUDE_PATH);
    $requestXml = str_replace("I2B2_URI", $uri, $requestXml);
    $requestXml = str_replace("I2B2_PARENT", $parent, $requestXml);
    $requestXml = str_replace("I2B2_MAX_RESULTS", $max_results, $requestXml);
    $requestXml = str_replace("I2B2_NUM_CHILDREN", $num_children, $requestXml);

    return $requestXml;
}
