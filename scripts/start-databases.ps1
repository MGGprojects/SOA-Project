$ErrorActionPreference = "Stop"

$composeFiles = @(
    "auth-service\docker\docker-compose.yml",
    "business-service\docker\docker-compose.yml",
    "event-service\docker\docker-compose.yml",
    "user-service\docker\docker-compose.yml",
    "notification-service\docker\docker-compose.yml"
)

$root = Split-Path -Parent $PSScriptRoot

foreach ($composeFile in $composeFiles) {
    $fullPath = Join-Path $root $composeFile
    Write-Host "Starting containers from $composeFile" -ForegroundColor Cyan
    docker compose -f $fullPath up -d
}

Write-Host ""
Write-Host "All database containers were started." -ForegroundColor Green
