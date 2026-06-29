# 从 deploy 模板初始化本地 pro/（勿提交 Git）
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Pro = Join-Path $Root "pro"

$services = @(
    @{ Name = "user-center"; EnvExample = "user-center.env.example"; EnvFile = "moli-user-center.env"; YamlExample = "user-center.yml.example" },
    @{ Name = "gateway";     EnvExample = "gateway.env.example";     EnvFile = "moli-gateway.env";     YamlExample = "gateway.yml.example" },
    @{ Name = "knowledge";   EnvExample = "knowledge.env.example";   EnvFile = "moli-knowledge.env";   YamlExample = "knowledge.yml.example" }
)

New-Item -ItemType Directory -Force -Path (Join-Path $Pro "linux") | Out-Null

foreach ($svc in $services) {
    $dir = Join-Path $Pro $svc.Name
    $conf = Join-Path $dir "conf"
    New-Item -ItemType Directory -Force -Path $conf | Out-Null

    $envSrc = Join-Path $PSScriptRoot "linux\$($svc.EnvExample)"
    $envDst = Join-Path $conf $svc.EnvFile
    if (-not (Test-Path $envDst)) {
        Copy-Item $envSrc $envDst
        Write-Host "created $envDst"
    } else {
        Write-Host "skip (exists) $envDst"
    }

    $yamlSrc = Join-Path $PSScriptRoot "application-pro\$($svc.YamlExample)"
    $yamlDst = Join-Path $dir "application-pro.yml"
    Copy-Item $yamlSrc $yamlDst -Force
    Write-Host "updated $yamlDst (from template)"
}

$shSrc = Join-Path $PSScriptRoot "linux\moli-service.sh"
$shDst = Join-Path $Pro "linux\moli-service.sh"
Copy-Item $shSrc $shDst -Force
Write-Host "copied moli-service.sh -> pro/linux/"

$readme = Join-Path $Pro "README.md"
@"
# 本地生产配置（gitignored）

由 ``deploy/setup-pro.ps1`` 生成。

| 文件 | 说明 |
|------|------|
| [上线流程.md](上线流程.md) | **生产上线步骤**（打包→上传→启停→冒烟→回滚） |
| ``*/conf/*.env`` | 环境变量（密码、Nacos、路径） |
| ``*/application-pro.yml`` | 生产 Spring 配置 |

Linux 部署根目录：``/opt/moli-project-distribute/``
"@ | Set-Content -Encoding UTF8 $readme

$runbookSrc = Join-Path $PSScriptRoot "上线流程.md.example"
$runbookDst = Join-Path $Pro "上线流程.md"
Copy-Item $runbookSrc $runbookDst -Force
Write-Host "updated $runbookDst"

Write-Host "Done. Edit pro/*/conf/*.env and pro/*/application-pro.yml before deploy."
