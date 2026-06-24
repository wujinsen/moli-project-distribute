# Export draw.io diagrams to PNG (requires draw.io Desktop)
param(
    [string]$DrawIo = "C:\Program Files\draw.io\draw.io.exe",
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
$DiagramDir = Join-Path $Root "docs\diagrams"
$OutDir = Join-Path $DiagramDir "png"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

if (-not (Test-Path $DrawIo)) {
    Write-Error @"
draw.io Desktop not found: $DrawIo
Install from https://github.com/jgraph/drawio-desktop/releases
Or export manually via https://app.diagrams.net/
"@
}

Get-ChildItem -Path $DiagramDir -Filter "*.drawio" | ForEach-Object {
    $out = Join-Path $OutDir ($_.BaseName + ".png")
    Write-Host "Export $($_.Name) -> png\$($_.BaseName).png"
    & $DrawIo --export --format png --scale 2 --border 10 -o $out $_.FullName
    if ($LASTEXITCODE -ne 0) { throw "Export failed: $($_.Name)" }
}

Write-Host "Done. Output: $OutDir"
