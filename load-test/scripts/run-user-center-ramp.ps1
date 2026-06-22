param(
    [string]$UcBaseUrl = "http://localhost:8888",
    [string]$LoginPassword = "123456",
    [string]$LoginUserPool = "zhangsan",
    [switch]$ViaGateway,
    [string]$GatewayUrl = "http://localhost:21000",
    [switch]$StressRamp,
    [string]$ReportDir = "",
    [switch]$NoHtmlReport
)

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LoadTestRoot = Split-Path -Parent $ScriptRoot

if (-not $ReportDir) {
    $ReportDir = Join-Path $LoadTestRoot "reports"
}
if (-not (Test-Path $ReportDir)) {
    New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null
}

$ReportName = "user-center-ramp-{0:yyyyMMdd-HHmmss}.html" -f (Get-Date)
$ReportPath = Join-Path $ReportDir $ReportName

$k6Args = @(
    "run",
    "-e", "LOGIN_PASSWORD=$LoginPassword",
    "-e", "LOGIN_USER_POOL=$LoginUserPool"
)

if ($StressRamp) {
    $k6Args += @("-e", "STRESS_RAMP=true")
    Write-Host "Stress ramp: up to ~5000 RPS (cluster / multi-instance recommended)"
} else {
    $k6Args += @("-e", "LOCAL_RAMP=true")
    Write-Host "Local ramp: up to ~300 RPS (single-machine friendly)"
}

if ($ViaGateway) {
    $k6Args += @("-e", "VIA_GATEWAY=true", "-e", "BASE_URL=$GatewayUrl")
} else {
    $k6Args += @("-e", "VIA_GATEWAY=false", "-e", "UC_BASE_URL=$UcBaseUrl")
}

if (-not $NoHtmlReport) {
    $k6Args += @("--out", "web-dashboard=export=$ReportPath&period=1s")
    Write-Host "Live dashboard: http://127.0.0.1:5665"
    Write-Host "HTML report: $ReportPath"
}

$k6Args += (Join-Path $ScriptRoot "..\k6\user-center-login-ramp.js")
& k6 @k6Args

if (-not $NoHtmlReport -and (Test-Path $ReportPath)) {
    Write-Host "Open report: $ReportPath"
    Start-Process $ReportPath
}
