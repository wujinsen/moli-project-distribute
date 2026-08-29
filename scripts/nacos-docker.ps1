# Local Nacos Docker helper (host ports 28548 / 29548)
# Usage:
#   .\scripts\nacos-docker.ps1          # start (default)
#   .\scripts\nacos-docker.ps1 start
#   .\scripts\nacos-docker.ps1 stop
#   .\scripts\nacos-docker.ps1 restart
#   .\scripts\nacos-docker.ps1 status
#   .\scripts\nacos-docker.ps1 logs

param(
    [ValidateSet("start", "stop", "restart", "status", "logs")]
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$ComposeDir = Join-Path $Root "deploy\docker"
$ComposeFile = Join-Path $ComposeDir "docker-compose.nacos.yml"
$NacosHostHttpPort = 28548
$Fmt = 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

function Show-NacosStatus {
    Write-Host ""
    docker ps -a --filter name=nacos --format $Fmt
}

function Ensure-NacosDevNamespace([int]$HostPort) {
    $deadline = (Get-Date).AddSeconds(90)
    while ((Get-Date) -lt $deadline) {
        try {
            $resp = Invoke-RestMethod -Uri "http://127.0.0.1:$HostPort/nacos/v1/console/namespaces" -TimeoutSec 5
            $hasDev = @($resp.data) | Where-Object { $_.namespace -eq "dev" }
            if ($hasDev) {
                Write-Host "[INFO] Nacos namespace 'dev' exists."
                return
            }
            curl.exe -s -X POST "http://127.0.0.1:$HostPort/nacos/v1/console/namespaces" `
                -d "customNamespaceId=dev&namespaceName=dev&namespaceDesc=local-dev" | Out-Null
            Write-Host "[INFO] Created Nacos namespace 'dev'."
            return
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    Write-Host "[WARN] Nacos API not ready; create namespace 'dev' manually in console."
}

function Start-Nacos {
    Push-Location $ComposeDir
    try {
        docker rm -f nacos 2>$null | Out-Null
        Write-Host "[INFO] Mapping ${NacosHostHttpPort}:8848 and 29548:9848"
        Write-Host "       Console: http://localhost:${NacosHostHttpPort}/nacos  (nacos/nacos)"
        Write-Host "       Java bootstrap default: http://127.0.0.1:${NacosHostHttpPort}"
        docker compose -f $ComposeFile up -d
        Ensure-NacosDevNamespace -HostPort $NacosHostHttpPort
        Show-NacosStatus
    } finally {
        Pop-Location
    }
}

function Stop-Nacos {
    Push-Location $ComposeDir
    try {
        $prev = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        docker compose -f $ComposeFile down 2>&1 | Out-Null
        $WinOverride = Join-Path $ComposeDir "docker-compose.nacos.windows.yml"
        docker compose -f $WinOverride down 2>&1 | Out-Null
        $ErrorActionPreference = $prev
    } finally {
        Pop-Location
    }
    docker rm -f nacos 2>$null | Out-Null
    Write-Host "[INFO] Nacos stopped."
    Show-NacosStatus
}

switch ($Action) {
    "start" { Start-Nacos }
    "stop" { Stop-Nacos }
    "restart" { Stop-Nacos; Start-Nacos }
    "status" { Show-NacosStatus }
    "logs" { docker logs -f nacos }
}
