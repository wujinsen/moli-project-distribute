# 软删 Web 直连 MySQL 遗留文档（source=manual 或无 slug 的 NULL source）
# 用法：
#   .\purge_manual_web.ps1              # 预览（dry-run，全空间）
#   .\purge_manual_web.ps1 -Execute     # 执行清理（全空间）
#   .\purge_manual_web.ps1 -Space enterprise-kb -Execute

param(
    [string]$Space = "",
    [switch]$Execute
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$py = Join-Path $PSScriptRoot "sync_to_db.py"

$args = @(
    "--host", "127.0.0.1",
    "--port", "3306",
    "--user", "root",
    "--password", "12345678",
    "--db", "moli",
    "--purge-manual-web"
)

if ($Space) {
    $args += @("--space", $Space)
} else {
    $args += "--all-spaces"
}

if (-not $Execute) {
    $args += "--dry-run"
    Write-Host "[purge_manual_web] dry-run preview (add -Execute to apply)" -ForegroundColor Yellow
} else {
    Write-Host "[purge_manual_web] applying soft-delete ..." -ForegroundColor Cyan
}

python $py @args
exit $LASTEXITCODE
