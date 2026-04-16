#Requires -Version 5.1
<#
.SYNOPSIS
    One-shot deploy: starts minikube, builds all images inside minikube's Docker
    daemon, applies the manifests, and opens the gateway in your browser.

.PARAMETER SkipBuild
    Skip the Docker build step (use when images are already loaded).

.PARAMETER SkipMinikube
    Skip the minikube start step (use when minikube is already running).
#>
param(
    [switch]$SkipBuild,
    [switch]$SkipMinikube
)

$ErrorActionPreference = "Stop"
$root   = Split-Path -Parent $PSScriptRoot
$ns     = "soa-local"
$yaml   = Join-Path $PSScriptRoot "local-stack.yaml"

function Step  ($msg) { Write-Host ""; Write-Host ">>> $msg" -ForegroundColor Yellow }
function Info  ($msg) { Write-Host "    $msg" -ForegroundColor Cyan }
function Ok    ($msg) { Write-Host "    OK: $msg" -ForegroundColor Green }
function Fail  ($msg) { Write-Host "    FAIL: $msg" -ForegroundColor Red; exit 1 }

function Invoke-Cmd {
    param([string]$Cmd, [string[]]$CmdArgs)
    & $Cmd @CmdArgs
    if ($LASTEXITCODE -ne 0) { Fail "Command failed: $Cmd $CmdArgs" }
}

function Invoke-CmdCapture {
    param([string]$Cmd, [string[]]$CmdArgs)

    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    $output = & $Cmd @CmdArgs 2>&1
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $prevEAP

    return [pscustomobject]@{
        Output   = ($output | Out-String)
        ExitCode = $exitCode
    }
}

function Test-DockerDaemon {
    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    $null = docker version --format '{{.Server.Version}}' 2>&1
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $prevEAP
    return ($exitCode -eq 0)
}

function Wait-ForDockerDaemon {
    param(
        [int]$TimeoutSeconds = 120,
        [int]$PollSeconds = 5
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-DockerDaemon) { return $true }
        Info "Docker daemon not ready yet; waiting ${PollSeconds}s..."
        Start-Sleep -Seconds $PollSeconds
    }

    return (Test-DockerDaemon)
}

function Start-MinikubeSafely {
    param(
        [int]$MemoryMb = 6144,
        [int]$CpuCount = 4
    )

    $startArgs = @("start", "--driver=docker", "--memory=$MemoryMb", "--cpus=$CpuCount")

    Info "Starting minikube (docker driver, 6 GB RAM, 4 CPUs)..."
    $startResult = Invoke-CmdCapture minikube $startArgs
    if ($startResult.ExitCode -eq 0) {
        if ($startResult.Output.Trim()) { Write-Host $startResult.Output.Trim() }
        return
    }

    if ($startResult.Output.Trim()) { Write-Host $startResult.Output.Trim() }

    Write-Host ""
    Write-Host "minikube start failed. Cleaning up any stale/broken local minikube state and retrying once..." -ForegroundColor Yellow

    $deleteResult = Invoke-CmdCapture minikube @("delete")
    if ($deleteResult.Output.Trim()) { Write-Host $deleteResult.Output.Trim() }
    if ($deleteResult.ExitCode -ne 0) {
        Fail "minikube delete failed while recovering from a broken cluster"
    }

    $retryResult = Invoke-CmdCapture minikube $startArgs
    if ($retryResult.Output.Trim()) { Write-Host $retryResult.Output.Trim() }
    if ($retryResult.ExitCode -ne 0) {
        Fail "minikube start failed even after deleting the broken cluster"
    }

    Ok "minikube recovered and started successfully"
}

# ── Step 1: minikube ─────────────────────────────────────────────────────────
Step "Checking minikube"

if (-not $SkipMinikube) {
    Step "Checking Docker daemon"
    if (-not (Wait-ForDockerDaemon -TimeoutSeconds 120 -PollSeconds 5)) {
        Write-Host "" 
        Write-Host "Docker CLI is installed, but the Docker daemon is not responding." -ForegroundColor Red
        Write-Host "Please make sure Docker Desktop is fully started before running this script." -ForegroundColor Yellow
        Write-Host "You can verify it manually with:" -ForegroundColor Yellow
        Write-Host "  docker version" -ForegroundColor White
        Write-Host "  docker info" -ForegroundColor White
        exit 1
    }
    Ok "Docker daemon is reachable"

    Step "Checking minikube"
    # minikube status exits non-zero when not running; capture without throwing.
    # Temporarily silence errors so PowerShell doesn't treat stderr output as a
    # terminating exception (relevant when the minikube Docker container is absent).
    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    $status = (minikube status --format='{{.Host}}' 2>&1) | Out-String
    $ErrorActionPreference = $prevEAP
    if ($status.Trim() -ne "Running") {
        Start-MinikubeSafely -MemoryMb 6144 -CpuCount 4
    } else {
        Ok "minikube is already running"
    }
} else {
    Ok "Skipping minikube start (-SkipMinikube)"
}

# ── Step 2: build images inside minikube's Docker daemon ─────────────────────
if (-not $SkipBuild) {
    Step "Configuring shell to use minikube's Docker daemon"

    # Capture the env vars minikube wants us to set.
    # Silence errors temporarily so stderr from minikube doesn't throw before
    # we can inspect $LASTEXITCODE ourselves.
    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    $envOutput = minikube docker-env --shell=cmd 2>&1
    $envExitCode = $LASTEXITCODE
    $ErrorActionPreference = $prevEAP
    if ($envExitCode -ne 0) { Fail "minikube docker-env failed" }

    # Parse and apply SET lines:  SET VAR=VALUE
    foreach ($line in $envOutput) {
        if ($line -match '^SET (\w+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
        }
    }
    Ok "Docker daemon pointed at minikube"

    $services = @(
        @{ Path = "auth-service";             Image = "soa/auth-service:local" },
        @{ Path = "business-service";         Image = "soa/business-service:local" },
        @{ Path = "event-service";            Image = "soa/event-service:local" },
        @{ Path = "user-service";             Image = "soa/user-service:local" },
        @{ Path = "calendar-export-service";  Image = "soa/calendar-export-service:local" },
        @{ Path = "notification-service";     Image = "soa/notification-service:local" },
        @{ Path = "gateway-service";          Image = "soa/gateway-service:local" }
    )

    foreach ($svc in $services) {
        Step "Building $($svc.Image)"
        $ctx = Join-Path $root $svc.Path
        Invoke-Cmd docker @("build", "-t", $svc.Image, $ctx)
        Ok "Built $($svc.Image)"
    }
} else {
    Ok "Skipping build (-SkipBuild)"
}

# ── Step 3: apply manifests ───────────────────────────────────────────────────
Step "Applying Kubernetes manifests"
Invoke-Cmd kubectl @("apply", "-f", $yaml)
Ok "Manifests applied"

# ── Step 4: wait for pods ─────────────────────────────────────────────────────
Step "Waiting for all pods to be ready (this takes 10-15 min on first run)"
Info "Tip: open another terminal and run:  kubectl get pods -n $ns -w"

$deployments = @(
    "rabbitmq",
    "auth-db", "business-db", "event-db", "user-db", "notification-db",
    "auth-service", "user-service", "business-service",
    "event-service", "calendar-export-service", "notification-service",
    "gateway-service"
)

foreach ($dep in $deployments) {
    Info "Waiting for $dep ..."
    kubectl rollout status deployment/$dep -n $ns --timeout=600s
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Deployment '$dep' did not become ready in time." -ForegroundColor Red
        Write-Host "Check logs with:" -ForegroundColor Yellow
        Write-Host "  kubectl logs -n $ns -l app=$dep --tail=40" -ForegroundColor White
        Write-Host "  kubectl describe pod -n $ns -l app=$dep" -ForegroundColor White
        exit 1
    }
    Ok "$dep is ready"
}

# ── Step 5: open the app ──────────────────────────────────────────────────────
Step "All pods are ready!"

Write-Host ""
Write-Host "To access the application, run this in a separate terminal:" -ForegroundColor Green
Write-Host "  kubectl port-forward -n $ns service/gateway-service 8080:8080" -ForegroundColor White
Write-Host "Then open: http://localhost:8080" -ForegroundColor White
Write-Host ""
