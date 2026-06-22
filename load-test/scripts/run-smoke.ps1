param(
    [string]$BaseUrl = "http://localhost:21000"
)

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
& k6 run -e "BASE_URL=$BaseUrl" (Join-Path $ScriptRoot "..\k6\seckill-smoke.js")
