<?php
require_once("db.php");
require_once("response.php");

$user_id = intval($_GET["user_id"] ?? 0);
$limit = intval($_GET["limit"] ?? 20);

if ($user_id <= 0) fail("user_id required");
if ($limit <= 0) $limit = 20;
if ($limit > 50) $limit = 50;

$host = (isset($_SERVER["HTTPS"]) && $_SERVER["HTTPS"] === "on" ? "https" : "http")
      . "://" . $_SERVER["HTTP_HOST"];
$base = $host . "/ocular/";

$query = "SELECT id, image_path, disease, confidence, severity, created_at 
          FROM history 
          WHERE user_id=? 
          ORDER BY id DESC 
          LIMIT ?";

$stmt = $conn->prepare($query);
$stmt->bind_param("ii", $user_id, $limit);
$stmt->execute();
$res = $stmt->get_result();

$list = [];
while ($row = $res->fetch_assoc()) {
    $image_path = $row["image_path"] ?? "";

    $image_url = "";
    if ($image_path !== "") {
        $image_url = $base . $image_path;
    }

    $list[] = [
        "id" => (string)$row["id"],
        "disease" => $row["disease"] ?? "",
        "confidence" => (int)($row["confidence"] ?? 0),
        "severity" => $row["severity"] ?? "",
        "image_url" => $image_url,
        "date" => date("d M Y", strtotime($row["created_at"])),
        "time" => date("h:i A", strtotime($row["created_at"]))
    ];
}

ok("History fetched", $list);
?>
