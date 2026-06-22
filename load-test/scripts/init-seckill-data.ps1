# Initialize seckill activity (Windows)
param(
    [string]$BaseUrl = "http://localhost:21000",
    [long]$ActivityId = 1,
    [long]$Stock = 1000000
)

$initUrl = "$BaseUrl/OrderServer/seckill/admin/init?activityId=$ActivityId&stock=$Stock&name=million-qps"
Write-Host "Init activity $ActivityId at $initUrl"
Invoke-RestMethod -Method Post -Uri $initUrl
Invoke-RestMethod -Method Get -Uri "$BaseUrl/OrderServer/seckill/activity/$ActivityId"
