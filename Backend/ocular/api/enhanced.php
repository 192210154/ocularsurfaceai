<?php
require_once("response.php");

if (!isset($_FILES["image"])) fail("No image uploaded");

$targetDir = "uploads/";
if (!is_dir($targetDir)) mkdir($targetDir, 0777, true);

$ext = pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION);
if ($ext === "") $ext = "jpg";

$filename = "enh_" . time() . "_" . rand(1000,9999) . "." . $ext;
$targetPath = $targetDir . $filename;

if (move_uploaded_file($_FILES["image"]["tmp_name"], $targetPath)) {
    ok("Enhanced uploaded", [
        "path" => "/ocular/uploads/" . $filename
    ]);
} else {
    fail("Upload failed", 500);
}
?>
