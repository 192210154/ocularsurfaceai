<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_POST["user_id"] ?? 0);
if ($user_id <= 0) fail("user_id required");
// accept 0/1
$dark_mode = (int)($_POST["dark_mode"] ?? 0);
$notifications = (int)($_POST["notifications"] ?? 1);
$auto_save = (int)($_POST["auto_save"] ?? 1);
$show_confidence = (int)($_POST["show_confidence"] ?? 1);

$dark_mode = $dark_mode ? 1 : 0;
$notifications = $notifications ? 1 : 0;
$auto_save = $auto_save ? 1 : 0;
$show_confidence = $show_confidence ? 1 : 0;

$stmt = mysqli_prepare($conn, "
	INSERT INTO user_settings(user_id, dark_mode, notifications, auto_save, show_confidence)
	VALUES (?,?,?,?,?)
	ON DUPLICATE KEY UPDATE
		dark_mode=VALUES(dark_mode),
		notifications=VALUES(notifications),
		auto_save=VALUES(auto_save),
		show_confidence=VALUES(show_confidence)
");
mysqli_stmt_bind_param($stmt, "iiiii", $user_id, $dark_mode, $notifications, $auto_save, $show_confidence);

if (!mysqli_stmt_execute($stmt)) fail("Failed to save settings", 500);
ok(["saved" => true]);

