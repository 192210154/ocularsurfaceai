<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_POST["user_id"] ?? 0);
$full_name = trim($_POST["full_name"] ?? "");

if ($user_id <= 0) fail("user_id required");
if ($full_name === "") fail("full_name required");

$stmt = mysqli_prepare($conn, "UPDATE users SET full_name=? WHERE id=?");
mysqli_stmt_bind_param($stmt, "si", $full_name, $user_id);

if (!mysqli_stmt_execute($stmt)) fail("Profile update failed", 500);
ok(["updated" => true]);

