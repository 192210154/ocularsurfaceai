<?php
require_once("db.php");
require_once("response.php");

$user_id = intval($_POST["user_id"] ?? 0);
$disease = $_POST["disease"] ?? "";
$confidence = intval($_POST["confidence"] ?? 0);
$severity = $_POST["severity"] ?? "";

if ($user_id == 0) fail("user_id required");
if ($disease == "") fail("disease required");
if (!isset($_FILES["image"])) fail("image required");

// upload image
$uploadDir = __DIR__ . "/../uploads/";
if (!file_exists($uploadDir)) mkdir($uploadDir, 0777, true);

$filename = "eye_" . time() . ".jpg";
$target = $uploadDir . $filename;

if (!move_uploaded_file($_FILES["image"]["tmp_name"], $target)) {
		fail("Upload failed");
}

$image_path = "uploads/" . $filename;

// save to DB
$stmt = $conn->prepare(""
INSERT INTO history (user_id, image_path, disease, confidence, severity, created_at)
VALUES (?, ?, ?, ?, ?, NOW())
""
);

$stmt->bind_param("issis", $user_id, $image_path, $disease, $confidence, $severity);

if (!$stmt->execute()) {
		fail("Failed to save result");
}

ok("Result saved");

