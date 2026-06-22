# Launch N k6 workers with execution segments (Windows PowerShell)
param(
    [int]$Workers = 10,
    [int]$TargetRps = 10000,
    [string]$BaseUrl = "http://localhost:21000",
    [string]$Duration = "5m"
)

$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$K6Script = Join-Path (Split-Path -Parent $ScriptRoot) "k6\seckill-million.js"
$LogDir = Join-Path (Split-Path -Parent $ScriptRoot) "results\$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$PerWorkerRps = [Math]::Max(1, [Math]::Floor($TargetRps / $Workers))
Write-Host "Launching $Workers workers at $PerWorkerRps RPS each (~$($PerWorkerRps * $Workers) aggregate)"

$jobs = @()
for ($i = 0; $i -lt $Workers; $i++) {
    $start = [double]$i / $Workers
    $end = [double]($i + 1) / $Workers
    $segment = "{0}:{1}:0" -f $start, $end
    $logFile = Join-Path $LogDir "worker-$i.json"
    Write-Host "Worker $i segment $segment"
    $jobs += Start-Job -ScriptBlock {
        param($seg, $seq, $base, $rps, $dur, $script, $out)
        k6 run --execution-segment $seg --execution-segment-sequence $seq `
            -e BASE_URL=$base -e TARGET_RPS=$rps -e DURATION=$dur `
            -o "json=$out" $script
    } -ArgumentList $segment, $i, $BaseUrl, $PerWorkerRps, $Duration, $K6Script, $logFile
}

$jobs | Wait-Job | Receive-Job
Write-Host "Done. Logs: $LogDir"
