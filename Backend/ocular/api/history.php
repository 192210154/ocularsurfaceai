<?php
require_once("db.php");
require_once("response.php");

$user_id = intval($_POST["user_id"] ?? 0);
$disease = trim($_POST["disease"] ?? "");
$confidence = intval($_POST["confidence"] ?? 0);
$severity = trim($_POST["severity"] ?? "");

if ($user_id <= 0) fail("user_id required");
if ($disease === "" || $severity === "") fail("disease and severity required");

if (!isset($_FILES["image"])) fail("image required");

// Upload to /ocular/uploads/
$uploadDir = __DIR__ . "/../uploads/";
if (!file_exists($uploadDir)) mkdir($uploadDir, 0777, true);

$ext = pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION);
if ($ext === "") $ext = "jpg";

$filename = "eye_" . time() . "_" . rand(1000,9999) . "." . $ext;
$target = $uploadDir . $filename;

if (!move_uploaded_file($_FILES["image"]["tmp_name"], $target)) {
    fail("Upload failed", 500);
}

$image_path = "uploads/" . $filename;

// FIX: put SQL into variable (VERY IMPORTANT)
$query = "INSERT INTO history (user_id, image_path, disease, confidence, severity, created_at)
          VALUES (?, ?, ?, ?, ?, NOW())";

$stmt = $conn->prepare($query);
$stmt->bind_param("issis", $user_id, $image_path, $disease, $confidence, $severity);

if (!$stmt->execute()) {
    fail("Failed to save result", 500);
}

ok("Result saved", [
    "id" => (string)$conn->insert_id,
    "image_path" => $image_path
]);
?>
