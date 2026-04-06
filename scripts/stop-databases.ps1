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
    Write-Host "Stopping containers from $composeFile" -ForegroundColor Yellow
    docker compose -f $fullPath down
}

Write-Host ""
Write-Host "All database containers were stopped." -ForegroundColor Green
