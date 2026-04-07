param(
    [string]$ServiceName
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$namespace = "soa-local"
$tag = Get-Date -Format "yyyyMMdd-HHmmss"

$services = @(
    @{ Path = "auth-service"; ImageName = "soa-project/auth-service"; Deployment = "auth-service"; Container = "auth-service" },
    @{ Path = "business-service"; ImageName = "soa-project/business-service"; Deployment = "business-service"; Container = "business-service" },
    @{ Path = "event-service"; ImageName = "soa-project/event-service"; Deployment = "event-service"; Container = "event-service" },
    @{ Path = "user-service"; ImageName = "soa-project/user-service"; Deployment = "user-service"; Container = "user-service" },
    @{ Path = "calendar-export-service"; ImageName = "soa-project/calendar-export-service"; Deployment = "calendar-export-service"; Container = "calendar-export-service" },
    @{ Path = "notification-service"; ImageName = "soa-project/notification-service"; Deployment = "notification-service"; Container = "notification-service" },
    @{ Path = "gateway-service"; ImageName = "soa-project/gateway-service"; Deployment = "gateway-service"; Container = "gateway-service" }
)

if ($ServiceName) {
    $services = $services | Where-Object { $_.Path -eq $ServiceName }

    if (-not $services) {
        throw "Service '$ServiceName' not found."
    }

    Write-Host "Filtering: only building $ServiceName" -ForegroundColor Green
}

Write-Host "Using tag: $tag" -ForegroundColor Green
Write-Host "Services to process:" -ForegroundColor Cyan
$services | ForEach-Object { Write-Host "- $($_.Path)" }

foreach ($service in $services) {
    $servicePath = Join-Path $root $service.Path
    $fullImage = "$($service.ImageName):$tag"
    $imageAssignment = "$($service.Container)=$fullImage"

    Write-Host ""
    Write-Host "Building $fullImage from $($service.Path)" -ForegroundColor Cyan
    docker build --no-cache -t $fullImage $servicePath

    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed for $fullImage"
    }

    Write-Host "Loading $fullImage into minikube..." -ForegroundColor Yellow
    minikube image load $fullImage

    if ($LASTEXITCODE -ne 0) {
        throw "minikube image load failed for $fullImage"
    }

    Write-Host "Updating deployment $($service.Deployment) to image $fullImage..." -ForegroundColor Magenta
    kubectl set image deployment $($service.Deployment) $imageAssignment -n $namespace

    if ($LASTEXITCODE -ne 0) {
        throw "kubectl set image failed for deployment/$($service.Deployment)"
    }

    Write-Host "Waiting for rollout of $($service.Deployment)..." -ForegroundColor DarkCyan
    kubectl rollout status deployment/$($service.Deployment) -n $namespace

    if ($LASTEXITCODE -ne 0) {
        throw "kubectl rollout status failed for deployment/$($service.Deployment)"
    }
}

Write-Host ""
Write-Host "Done." -ForegroundColor Green