# Start Prometheus + Grafana (host services must use loadtest profile)
param(
    [switch]$Detach
)

$ComposeFile = Join-Path (Split-Path -Parent $PSScriptRoot) "docker\docker-compose.monitoring.yml"
$Args = @("-f", $ComposeFile, "up")
if ($Detach) { $Args += "-d" }
docker compose @Args
