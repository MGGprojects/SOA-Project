$ErrorActionPreference = "Stop"

$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 8086)
$root = Split-Path -Parent $PSScriptRoot

function Stop-ProcessesOnPort {
    param (
        [int]$Port
    )

    $lines = netstat -ano | Select-String ":$Port\s+.*LISTENING\s+(\d+)$"
    $pids = @()

    foreach ($line in $lines) {
        if ($line.Matches.Count -gt 0) {
            $pidValue = [int]$line.Matches[0].Groups[1].Value
            if ($pidValue -gt 0 -and $pids -notcontains $pidValue) {
                $pids += $pidValue
            }
        }
    }

    foreach ($pidValue in $pids) {
        Write-Host "Stopping PID $pidValue on port $Port" -ForegroundColor Yellow
        taskkill /PID $pidValue /F | Out-Null
    }
}

Write-Host "Stopping service processes..." -ForegroundColor Cyan
foreach ($port in $ports) {
    Stop-ProcessesOnPort -Port $port
}

Write-Host ""
Write-Host "Restarting database containers..." -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "stop-databases.ps1")
& (Join-Path $PSScriptRoot "start-databases.ps1")

Write-Host ""
Write-Host "Starting demo services in new windows..." -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "start-demo-services.ps1")

Write-Host ""
Write-Host "Full demo restart complete." -ForegroundColor Green
