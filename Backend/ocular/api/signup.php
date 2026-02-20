<?php
require_once("db.php");
require_once("response.php");

/**
 * Read JSON input
 */
$input = json_decode(file_get_contents("php://input"), true);

$name = trim($input["name"] ?? "");
$email = trim($input["email"] ?? "");
$password = trim($input["password"] ?? "");
$gender = trim($input["gender"] ?? "");
$qualification = trim($input["qualification"] ?? "");

/**
 * Validate input
 */
if ($name === "" || $email === "" || $password === "" || $gender === "" || $qualification === "") {
    fail("All fields required");
}

/**
 * Check email exists
 */
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$res = $stmt->get_result();

if ($res->num_rows > 0) {
    fail("Email already exists");
}

/**
 * Hash password
 */
$hash = password_hash($password, PASSWORD_BCRYPT);

/**
 * Insert user (WITH gender & qualification)
 */
$stmt = $conn->prepare("
    INSERT INTO users (name, email, password_hash, gender, qualification)
    VALUES (?, ?, ?, ?, ?)
");

$stmt->bind_param("sssss", $name, $email, $hash, $gender, $qualification);

if ($stmt->execute()) {
    ok("Signup successful", [
        "user_id" => (string) $stmt->insert_id,
        "name" => $name,
        "email" => $email,
        "gender" => $gender,
        "qualification" => $qualification
    ]);
} else {
    fail("Signup failed");
}
