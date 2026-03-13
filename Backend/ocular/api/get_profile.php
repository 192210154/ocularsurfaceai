<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_GET["user_id"] ?? 0);
if ($user_id <= 0) fail("user_id required");

$stmt = mysqli_prepare($conn, "SELECT id, full_name, email, created_at FROM users WHERE id=? LIMIT 1");
mysqli_stmt_bind_param($stmt, "i", $user_id);
mysqli_stmt_execute($stmt);
$res = mysqli_stmt_get_result($stmt);
$row = mysqli_fetch_assoc($res);

if (!$row) fail("User not found", 404);
ok($row);

