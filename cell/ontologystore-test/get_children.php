<?php
require './functions.php';

$parents = array(
    "\\\ACT\Medications\MedicationsByVaClass\V2_09302018\VA000\CN000\CN600\\",
    "\\\i2b2_LABS\i2b2\Labtests\LAB\(LLB16) Chemistry\\",
    "\\\i2b2_DEMO\i2b2\Demographics\Gender\\"
);

$parent = $parents[1];
$num_children = 1;
$max_results = 9999;

$response = getChildren($parent, $num_children, $max_results);
?>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Get Children Test</title>
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="vendor/highlight/styles/default.min.css" />
    </head>
    <body class="bg-light">
        <div class="container py-4">
            <header class="border-bottom">
                <span class="fs-4">Get Children</span>
            </header>
            <pre>
                <code class="language-xml"><?php echo htmlspecialchars($response, ENT_XML1); ?></code>
            </pre>
            <footer class="text-muted border-top"></footer>
        </div>
        <script src="vendor/highlight/highlight.min.js"></script>
        <script>hljs.highlightAll();</script>
    </body>
</html>
