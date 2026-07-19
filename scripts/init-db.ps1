# Import scripts/moli.sql (+ optional seckill / knowledge tables) on Windows
# All SQL imports use: mysql --default-character-set=utf8mb4 + "source <file>"
# Do NOT pipe UTF-8 SQL via Get-Content | mysql (Chinese becomes '?').
# See docs/sql/README.md and scripts/README.md.

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

$KnowledgeOpsSpaceSql = Join-Path $Root "docs\sql\07_kb_space_ops_manual.sql"



function Import-SqlFile {

    param([string]$Path, [string]$Label)

    if (-not (Test-Path $Path)) { return }

    Write-Host "Importing $Label ..."

    $sourcePath = ($Path -replace '\\', '/')

    & $Mysql -u $User "-p$Password" --default-character-set=utf8mb4 $Database -e "source $sourcePath"

    if ($LASTEXITCODE -ne 0) { throw "$Label import failed" }

}



if (-not (Test-Path $Mysql)) {

    Write-Error "mysql.exe not found: $Mysql"

}

if (-not (Test-Path $MoliSql)) {

    Write-Error "Missing $MoliSql — copy from moli-project-single/scripts/moli.sql"

}



Write-Host "Creating database $Database ..."

& $Mysql -u $User "-p$Password" --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS ``$Database`` DEFAULT CHARSET utf8mb4;"



Import-SqlFile -Path $MoliSql -Label "scripts/moli.sql (latest full dump)"



if (-not $SkipSeckill) {

    Import-SqlFile -Path $SeckillSql -Label "docs/sql/02_seckill_schema.sql"

}



if (-not $SkipKnowledge) {

    Import-SqlFile -Path $KnowledgeSql -Label "docs/sql/03_knowledge_schema.sql"

    Import-SqlFile -Path $KnowledgeMenuSql -Label "docs/sql/04_knowledge_menu.sql"

    Import-SqlFile -Path $KnowledgeOpsSpaceSql -Label "docs/sql/07_kb_space_ops_manual.sql"

    $KbEvalRunSql = Join-Path $Root "docs\sql\31_kb_eval_run.sql"
    Import-SqlFile -Path $KbEvalRunSql -Label "docs/sql/31_kb_eval_run.sql (AI-3 eval, idempotent)"

    $AiChatTraceSql = Join-Path $Root "docs\sql\32_ai_chat_trace.sql"
    Import-SqlFile -Path $AiChatTraceSql -Label "docs/sql/32_ai_chat_trace.sql (AI-4 ChatBI trace + ro user)"

}



Write-Host "Done. Verify users:"

& $Mysql -u $User "-p$Password" --default-character-set=utf8mb4 $Database -e "SELECT user_name, status FROM sys_user LIMIT 5;"

