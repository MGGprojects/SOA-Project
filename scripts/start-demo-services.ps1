$ErrorActionPreference = "Stop"

$services = @(
    @{
        Name = "auth-service"
        Path = "auth-service"
    },
    @{
        Name = "business-service"
        Path = "business-service"
    },
    @{
        Name = "event-service"
        Path = "event-service"
    },
    @{
        Name = "user-service"
        Path = "user-service"
    },
    @{
        Name = "calendar-export-service"
        Path = "calendar-export-service"
    },
    @{
        Name = "notification-service"
        Path = "notification-service"
    },
    @{
        Name = "gateway-service"
        Path = "gateway-service"
    }
)

$root = Split-Path -Parent $PSScriptRoot

foreach ($service in $services) {
    $servicePath = Join-Path $root $service.Path
    Write-Host "Opening $($service.Name) in a new PowerShell window..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$servicePath'; .\mvnw.cmd spring-boot:run"
}

Write-Host ""
Write-Host "Demo services are starting in separate windows." -ForegroundColor Green
