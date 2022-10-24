<?php
require './functions.php';

$response = performProductActions();
?>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Product Action Test</title>
        <link rel="stylesheet" href="vendor/bootstrap/css/bootstrap.min.css" />
        <link rel="stylesheet" href="vendor/highlight/styles/default.min.css" />
    </head>
    <body class="bg-light">
        <div class="container py-4">
            <header class="border-bottom">
                <span class="fs-4">Product Action</span>
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
