# AI-2 W3 整改复测脚本（索引 → 三档 eval）
# 前置：MySQL/Redis 正常；以管理员重启 MySQL80 若连接超时
# 用法：在 moli-knowledge 目录下
#   powershell -ExecutionPolicy Bypass -File kb/tools/run_ai2_remediation.ps1

$ErrorActionPreference = "Stop"
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$Kb = Join-Path $Root "moli-knowledge"

function Test-Http($url, $timeoutSec = 5) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec $timeoutSec
        return $r.StatusCode -eq 200
    } catch { return $false }
}

Write-Host "== 1/4 探活 =="
if (-not (Test-Http "http://127.0.0.1:8099/health")) {
    Write-Host "[error] sidecar 8099 未就绪；先启动 kb-retrieval"
    exit 2
}
if (-not (Test-Http "http://127.0.0.1:8090/kb/ask/llm-config")) {
    Write-Host "[error] knowledge-server 8090 未就绪"
    exit 2
}
try {
    $login = Invoke-WebRequest -Uri "http://127.0.0.1:8888/login" -Method POST `
        -ContentType "application/json" -Body '{"userName":"admin","password":"123456"}' `
        -UseBasicParsing -TimeoutSec 15
    if ($login.StatusCode -ne 200) { throw "login status $($login.StatusCode)" }
} catch {
    Write-Host "[error] user-center 8888 登录失败：$($_.Exception.Message)"
    Write-Host "        请重启 MySQL80 + user-center-server 后再跑"
    exit 2
}

Write-Host "== 2/4 全量索引（--no-reconcile 避免 Chroma 锁） =="
Push-Location $Kb
python -u kb/tools/build_vector_index.py --batch-size 64 --no-reconcile
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }

Write-Host "== 3/4 三档 eval =="
foreach ($s in @("ngram", "hybrid", "hybrid-rerank")) {
    python kb/tools/eval_ask.py --strategy $s --kb-base http://127.0.0.1:8090
    if ($LASTEXITCODE -ne 0) { Write-Host "[warn] $s eval exit $LASTEXITCODE" }
}

Write-Host "== 4/4 报告目录 =="
Get-ChildItem kb/eval/reports/ai2-compare-*.json | Sort-Object LastWriteTime -Descending | Select-Object -First 3
Pop-Location
Write-Host "完成。请 Opus 复核 §4 后再改契约 status: done"
