# Export draw.io diagrams to PNG
param(
    [string]$DrawIo = "C:\Program Files\draw.io\draw.io.exe",
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
$DiagramDir = Join-Path $Root "docs\diagrams"
$OutDir = Join-Path $DiagramDir "png"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$useDesktop = Test-Path $DrawIo

if (-not $useDesktop) {
    Write-Host "draw.io Desktop not found; using npx draw.io-export"
}

Get-ChildItem -Path $DiagramDir -Filter "*.drawio" | ForEach-Object {
    $out = Join-Path $OutDir ($_.BaseName + ".png")
    Write-Host "Export $($_.Name) -> png\$($_.BaseName).png"
    if ($useDesktop) {
        & $DrawIo --export --format png --scale 2 --border 10 -o $out $_.FullName
        if ($LASTEXITCODE -ne 0) { throw "Export failed: $($_.Name)" }
    } else {
        npx --yes draw.io-export $_.FullName -o $out
        if ($LASTEXITCODE -ne 0) { throw "npx export failed: $($_.Name)" }
    }
}

Write-Host "Done. Output: $OutDir"
