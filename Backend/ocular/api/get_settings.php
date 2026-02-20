<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_GET["user_id"] ?? 0);
if ($user_id <= 0) fail("user_id required");
// ensure row exists
mysqli_query($conn, "INSERT IGNORE INTO user_settings(user_id) VALUES ($user_id)");

$stmt = mysqli_prepare($conn, "SELECT dark_mode, notifications, auto_save, show_confidence FROM user_settings WHERE user_id=? LIMIT 1");
mysqli_stmt_bind_param($stmt, "i", $user_id);
mysqli_stmt_execute($stmt);
$res = mysqli_stmt_get_result($stmt);
$row = mysqli_fetch_assoc($res);

ok($row ?: ["dark_mode"=>0,"notifications"=>1,"auto_save"=>1,"show_confidence"=>1]);

