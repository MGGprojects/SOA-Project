# Local Kubernetes Setup

One-command deploy to a local minikube cluster.

## Files

| File | Purpose |
|------|---------|
| `local-stack.yaml` | All Kubernetes manifests (namespace, secrets, databases, services) |
| `deploy.ps1` | **Main script** — starts minikube, builds images, deploys, waits for readiness |
| `teardown.ps1` | Tears down all resources (optionally deletes minikube) |

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — must be **running**
- [minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

---

## Deploy (one command)

```powershell
.\k8s\deploy.ps1
```

This script will:
1. Start minikube (if not already running)
2. Point Docker at minikube's internal daemon
3. Build all 7 service images directly inside minikube
4. Apply all Kubernetes manifests
5. Wait for every pod to become ready
6. Open the gateway URL in your browser

> **First run takes 10–15 minutes** — Maven downloads dependencies and Spring Boot
> starts slowly on limited resources. Subsequent runs are faster (Maven cache is warm).

---

## Accessing the application

### Option A — port-forward (recommended, always works)

```powershell
kubectl port-forward -n soa-local service/gateway-service 8080:8080
```

Then open: **http://localhost:8080**

Keep the terminal open while you use the app.

### Option B — NodePort (may not work on all machines with docker driver)

```powershell
kubectl get node minikube -o jsonpath='{.status.addresses[0].address}'
# → e.g. 192.168.49.2
```

Then open: `http://<node-ip>:30080`

---

## Check pod status

```powershell
kubectl get pods -n soa-local
```

All pods should show `1/1 Running`. Example healthy output:

```
NAME                                       READY   STATUS    RESTARTS   AGE
auth-db-...                                1/1     Running   0          5m
auth-service-...                           1/1     Running   0          5m
business-db-...                            1/1     Running   0          5m
business-service-...                       1/1     Running   0          5m
calendar-export-service-...                1/1     Running   0          5m
event-db-...                               1/1     Running   0          5m
event-service-...                          1/1     Running   0          5m
gateway-service-...                        1/1     Running   0          5m
notification-db-...                        1/1     Running   0          5m
notification-service-...                   1/1     Running   0          5m
rabbitmq-...                               1/1     Running   0          5m
user-db-...                                1/1     Running   0          5m
user-service-...                           1/1     Running   0          5m
```

---

## Tear down

```powershell
# Remove all Kubernetes resources (keep minikube running)
.\k8s\teardown.ps1

# Remove all resources AND delete the minikube cluster
.\k8s\teardown.ps1 -DeleteMinikube
```

---

## Rebuild a single service

If you change code in one service and want to redeploy just that service:

```powershell
# 1. Point Docker at minikube's daemon
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# 2. Rebuild the image (example: auth-service)
docker build -t soa/auth-service:local .\auth-service\

# 3. Restart the deployment to pick up the new image
kubectl rollout restart deployment/auth-service -n soa-local
```

---

## Troubleshooting

### Pods stuck in `0/1 Running` (readiness probe failing)

Spring Boot services take **5–8 minutes** to start on first run. Check if the service
has actually started:

```powershell
kubectl logs -n soa-local -l app=auth-service --tail=5
```

Look for `Started AuthServiceApplication in ... seconds`. If you see it, the pod will
become ready on the next probe check (every 15 seconds).

### `ImagePullBackOff` or `ErrImagePull`

Images were not built inside minikube's Docker daemon. Run the full deploy script:

```powershell
.\k8s\deploy.ps1
```

### Pods stuck in `Pending`

Minikube is out of resources. Delete and recreate with more memory:

```powershell
minikube delete
minikube start --driver=docker --memory=6144 --cpus=4
.\k8s\deploy.ps1 -SkipMinikube
```

### `CrashLoopBackOff`

Check the logs:

```powershell
kubectl logs -n soa-local <pod-name>
kubectl logs -n soa-local <pod-name> --previous
```

### minikube won't start / broken cluster

```powershell
minikube delete
.\k8s\deploy.ps1
```
