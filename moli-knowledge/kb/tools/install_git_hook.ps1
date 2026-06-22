# Git post-commit hook installer (Windows PowerShell)
# Usage: powershell -ExecutionPolicy Bypass -File moli-knowledge/kb/tools/install_git_hook.ps1

$ErrorActionPreference = "Stop"
$root = git rev-parse --show-toplevel
$hookDir = Join-Path $root ".git/hooks"
$hookPath = Join-Path $hookDir "post-commit"
$source = Join-Path $root "moli-knowledge/kb/tools/post-commit-kb-sync.sh"

if (-not (Test-Path $source)) {
    Write-Error "Hook template not found: $source"
}

Copy-Item $source $hookPath -Force
# Git Bash 需要可执行位；Windows 下直接调用 bash 也行
Write-Host "Installed post-commit hook -> $hookPath"
Write-Host "Wiki changes under moli-knowledge/kb/wiki/ will trigger sync_to_db.py after commit."
