param(
    [string]$UcBaseUrl = "http://localhost:8888",
    [string]$LoginPassword = "",
    [switch]$ViaGateway,
    [string]$GatewayUrl = "http://localhost:21000",
    [string]$ReportDir = "",
    [string]$ReportName = "",
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

if (-not $ReportName) {
    $ReportName = "user-center-smoke-{0:yyyyMMdd-HHmmss}.html" -f (Get-Date)
}
$ReportPath = Join-Path $ReportDir $ReportName

$k6Args = @("run")
if ($LoginPassword) {
    $k6Args += @("-e", "LOGIN_PASSWORD=$LoginPassword")
}

if ($ViaGateway) {
    $k6Args += @("-e", "VIA_GATEWAY=true", "-e", "BASE_URL=$GatewayUrl")
} else {
    $k6Args += @("-e", "VIA_GATEWAY=false", "-e", "UC_BASE_URL=$UcBaseUrl")
}

# 冒烟默认单用户，避免 LOGIN_USER_POOL 里未同步密码的用户拉低通过率
$k6Args += @("-e", "LOGIN_USER_POOL=zhangsan")

if (-not $NoHtmlReport) {
    # 压测中实时看板 http://127.0.0.1:5665 ；结束后导出 HTML
    $k6Args += @("--out", "web-dashboard=export=$ReportPath&period=1s")
    Write-Host "Live dashboard (during run): http://127.0.0.1:5665"
    Write-Host "HTML report will be saved to: $ReportPath"
}

$k6Args += (Join-Path $ScriptRoot "..\k6\user-center-login-smoke.js")
& k6 @k6Args

if (-not $NoHtmlReport -and (Test-Path $ReportPath)) {
    Write-Host ""
    Write-Host "Open report: $ReportPath"
    if ($env:OS -match "Windows") {
        Start-Process $ReportPath
    }
}
