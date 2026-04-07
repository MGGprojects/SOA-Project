# Local Kubernetes Setup

This folder contains a simple local Kubernetes setup for the project.

## Goal

The purpose is to provide a minimal local Kubernetes deployment that covers:

- all main microservices
- one PostgreSQL instance per stateful service
- RabbitMQ for messaging
- the API gateway as the main entry point

This setup is intentionally simple and aimed at demos or coursework, not production.

## What is included

- `local-stack.yaml`
- `build-local-images.ps1`
- `apply-local.ps1`
- `delete-local.ps1`

## Build local images

From the project root:

```powershell
.\k8s\build-local-images.ps1
```

## Apply manifests

```powershell
.\k8s\apply-local.ps1
```

## Access the gateway

The gateway is exposed as a NodePort service on port `30080`.

If your local cluster does not expose NodePorts directly, use:

```powershell
kubectl port-forward -n soa-local service/gateway-service 8080:8080
```

Then open:

- `http://localhost:8080`

## Delete everything

```powershell
.\k8s\delete-local.ps1
```
