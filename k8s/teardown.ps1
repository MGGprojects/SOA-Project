#Requires -Version 5.1
<#
.SYNOPSIS
    Tears down the local Kubernetes deployment.

.PARAMETER DeleteMinikube
    Also delete the minikube cluster entirely (full reset).
#>
param(
    [switch]$DeleteMinikube
)

$ErrorActionPreference = "Stop"
$yaml = Join-Path $PSScriptRoot "local-stack.yaml"
$ns   = "soa-local"

Write-Host ""
Write-Host "Deleting all resources in namespace '$ns'..." -ForegroundColor Yellow
kubectl delete -f $yaml --ignore-not-found=true
Write-Host "Resources deleted." -ForegroundColor Green

if ($DeleteMinikube) {
    Write-Host ""
    Write-Host "Deleting minikube cluster..." -ForegroundColor Yellow
    minikube delete
    Write-Host "Minikube cluster deleted." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Minikube is still running. To stop it:  minikube stop" -ForegroundColor DarkGray
    Write-Host "To delete it entirely:  .\k8s\teardown.ps1 -DeleteMinikube" -ForegroundColor DarkGray
}

Write-Host ""
