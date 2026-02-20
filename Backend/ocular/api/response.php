<?php
function ok($message, $data = null) {
    echo json_encode([
        "success" => true,
        "message" => $message,
        "data" => $data
    ]);
    exit();
}

function fail($message, $code = 400) {
    http_response_code($code);
    echo json_encode([
        "success" => false,
        "message" => $message,
        "data" => null
    ]);
    exit();
}
?>
