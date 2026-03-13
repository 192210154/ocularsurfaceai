<?php
require_once "db.php";
require_once "response.php";

$user_id = (int)($_POST["user_id"] ?? 0);
if ($user_id <= 0) fail("user_id required");
// collect image paths
$stmt = mysqli_prepare($conn, "SELECT image_path FROM analysis_history WHERE user_id=?");
mysqli_stmt_bind_param($stmt, "i", $user_id);
mysqli_stmt_execute($stmt);
$res = mysqli_stmt_get_result($stmt);

$paths = [];
while ($r = mysqli_fetch_assoc($res)) {
	$paths[] = $r["image_path"];
}
// delete user (history will be deleted if FK cascade is set)
// If not cascade, delete history first:
mysqli_query($conn, "DELETE FROM analysis_history WHERE user_id=$user_id");
mysqli_query($conn, "DELETE FROM user_settings WHERE user_id=$user_id");

$stmt2 = mysqli_prepare($conn, "DELETE FROM users WHERE id=?");
mysqli_stmt_bind_param($stmt2, "i", $user_id);
mysqli_stmt_execute($stmt2);
// delete files
foreach ($paths as $p) {
	// if stored like "uploads/xxx.jpg"
	$full = __DIR__ . "/../" . $p;
	if (file_exists($full)) @unlink($full);
}

ok(["deleted" => true]);

