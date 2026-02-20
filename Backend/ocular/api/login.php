<?php
require_once("db.php");
require_once("response.php");

$input = json_decode(file_get_contents("php://input"), true);

$email = trim($input["email"] ?? "");
$password = trim($input["password"] ?? "");

if ($email === "" || $password === "") {
    fail("Email and password required");
}

$stmt = $conn->prepare("SELECT id, name, email, password_hash FROM users WHERE email=?");
$stmt->bind_param("s", $email);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows === 0) {
    fail("Invalid credentials");
}

$user = $res->fetch_assoc();

if (!password_verify($password, $user["password_hash"])) {
    fail("Invalid credentials");
}

ok("Login successful", [
    "user_id" => (string)$user["id"],
    "name" => $user["name"],
    "email" => $user["email"]
]);
?>
