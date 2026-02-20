<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_POST["user_id"] ?? 0);
$current = $_POST["current_password"] ?? "";
$newpass = $_POST["new_password"] ?? "";

if ($user_id <= 0) fail("user_id required");
if ($current === "" || $newpass === "") fail("current_password and new_password required");
if (strlen($newpass) < 6) fail("Password must be at least 6 characters");

$stmt = mysqli_prepare($conn, "SELECT password_hash FROM users WHERE id=? LIMIT 1");
mysqli_stmt_bind_param($stmt, "i", $user_id);
mysqli_stmt_execute($stmt);
$res = mysqli_stmt_get_result($stmt);
$row = mysqli_fetch_assoc($res);

if (!$row) fail("User not found", 404);
if (!password_verify($current, $row["password_hash"])) fail("Current password is incorrect", 401);

$newhash = password_hash($newpass, PASSWORD_BCRYPT);
$stmt2 = mysqli_prepare($conn, "UPDATE users SET password_hash=? WHERE id=?");
mysqli_stmt_bind_param($stmt2, "si", $newhash, $user_id);

if (!mysqli_stmt_execute($stmt2)) fail("Password update failed", 500);
ok(["updated" => true]);

