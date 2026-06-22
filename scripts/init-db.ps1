# Import scripts/moli.sql (+ optional seckill tables) on Windows
param(
    [string]$Mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    [string]$User = "root",
    [string]$Password = "12345678",
    [string]$Database = "moli",
    [switch]$SkipSeckill,
    [switch]$SkipKnowledge
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$MoliSql = Join-Path $Root "scripts\moli.sql"
$SeckillSql = Join-Path $Root "docs\sql\02_seckill_schema.sql"
$KnowledgeSql = Join-Path $Root "docs\sql\03_knowledge_schema.sql"
$KnowledgeMenuSql = Join-Path $Root "docs\sql\04_knowledge_menu.sql"

if (-not (Test-Path $Mysql)) {
    Write-Error "mysql.exe not found: $Mysql"
}
if (-not (Test-Path $MoliSql)) {
    Write-Error "Missing $MoliSql — copy from moli-project-single/scripts/moli.sql"
}

Write-Host "Creating database $Database ..."
& $Mysql -u $User "-p$Password" -e "CREATE DATABASE IF NOT EXISTS ``$Database`` DEFAULT CHARSET utf8mb4;"

Write-Host "Importing scripts/moli.sql (latest full dump) ..."
Get-Content $MoliSql -Raw -Encoding UTF8 | & $Mysql -u $User "-p$Password" $Database
if ($LASTEXITCODE -ne 0) { throw "moli.sql import failed" }

if (-not $SkipSeckill) {
    if (Test-Path $SeckillSql) {
        Write-Host "Importing docs/sql/02_seckill_schema.sql ..."
        Get-Content $SeckillSql -Raw -Encoding UTF8 | & $Mysql -u $User "-p$Password" $Database
    }
}

if (-not $SkipKnowledge) {
    if (Test-Path $KnowledgeSql) {
        Write-Host "Importing docs/sql/03_knowledge_schema.sql ..."
        Get-Content $KnowledgeSql -Raw -Encoding UTF8 | & $Mysql -u $User "-p$Password" $Database
    }
    if (Test-Path $KnowledgeMenuSql) {
        Write-Host "Importing docs/sql/04_knowledge_menu.sql ..."
        Get-Content $KnowledgeMenuSql -Raw -Encoding UTF8 | & $Mysql -u $User "-p$Password" $Database
    }
}

Write-Host "Done. Verify users:"
& $Mysql -u $User "-p$Password" $Database -e "SELECT user_name, status FROM sys_user LIMIT 5;"
