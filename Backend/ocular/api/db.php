<?php
header("Content-Type: application/json; charset=UTF-8");

$host = "localhost";
$user = "root";
$pass = "";          // put your mysql password if any
$db   = "ocular_app";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "DB connection failed: " . $conn->connect_error,
        "data" => null
    ]);
    exit();
}
?>
