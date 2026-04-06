$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$services = @(
    @{ Path = "auth-service"; Image = "soa-project/auth-service:local" },
    @{ Path = "business-service"; Image = "soa-project/business-service:local" },
    @{ Path = "event-service"; Image = "soa-project/event-service:local" },
    @{ Path = "user-service"; Image = "soa-project/user-service:local" },
    @{ Path = "calendar-export-service"; Image = "soa-project/calendar-export-service:local" },
    @{ Path = "notification-service"; Image = "soa-project/notification-service:local" },
    @{ Path = "gateway-service"; Image = "soa-project/gateway-service:local" }
)

foreach ($service in $services) {
    $servicePath = Join-Path $root $service.Path
    Write-Host "Building $($service.Image) from $($service.Path)" -ForegroundColor Cyan
    docker build -t $service.Image $servicePath

    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed for $($service.Image)"
    }
}

Write-Host ""
Write-Host "All local images were built." -ForegroundColor Green
