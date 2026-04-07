# Local Kubernetes Setup

This folder contains a simple local Kubernetes setup for the project.

## What is included

- `local-stack.yaml` — all Kubernetes manifests (namespace, secrets, databases, services)
- `build-local-images.ps1` — builds all Docker images and loads them into minikube
- `apply-local.ps1` — applies all manifests to the cluster
- `delete-local.ps1` — tears down everything

---

## Prerequisites

Make sure you have the following installed and running before you start:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — must be **running**
- [minikube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

---

## First-time setup (and after every rebuild)

Run these three steps **in order** from the project root:

### Step 1 — Start minikube

```powershell
minikube start --driver=docker --memory=6100 --cpus=2
```

> If minikube was already running but broken (e.g. apiserver stopped), delete it first:
> ```powershell
> minikube delete
> minikube start --driver=docker --memory=6100 --cpus=2
> ```

### Step 2 — Build images and load them into minikube

```powershell
.\k8s\build-local-images.ps1
```

> **Important:** This step both builds the Docker images AND loads them into minikube's
> internal registry. You must run this script (not plain `docker build`) because minikube
> uses its own isolated Docker daemon — images built with plain `docker build` are invisible
> to minikube and will cause `ImagePullBackOff` errors.

### Step 3 — Deploy to Kubernetes

```powershell
.\k8s\apply-local.ps1
```

---

## Accessing the gateway

The services take a few minutes to start (Spring Boot startup on limited resources can take
**5–10 minutes** for all pods). Wait until the gateway pod is ready before trying to connect.

### Check pod status

```powershell
kubectl get pods -n soa-local
```

Wait until all pods show `1/1 Running`. Example of a healthy cluster:

```
NAME                                       READY   STATUS    RESTARTS   AGE
auth-db-...                                1/1     Running   0          5m
auth-service-...                           1/1     Running   0          5m
business-db-...                            1/1     Running   0          5m
business-service-...                       1/1     Running   0          5m
...
gateway-service-...                        1/1     Running   0          5m
```

### Forward the gateway port

Once all pods are `Running`, open a terminal and run (keep it open):

```powershell
kubectl port-forward -n soa-local service/gateway-service 8080:8080
```

Then open: **http://localhost:8080**

> If you get `error: unable to forward port because pod is not running`, the pod is still
> starting up. Wait a minute and try again.
>
> If you get `Connection refused`, the Spring Boot app inside the pod is still initializing.
> Check the logs with:
> ```powershell
> kubectl logs -n soa-local -l app=gateway-service --tail=20
> ```
> Wait until you see `Started GatewayServiceApplication in ... seconds`, then retry the
> port-forward.

---

## Tear down

To delete all deployed resources:

```powershell
.\k8s\delete-local.ps1
```

To also stop minikube:

```powershell
minikube stop
```

To completely wipe minikube (use this if minikube is broken):

```powershell
minikube delete
```

---

## Troubleshooting

### `ImagePullBackOff` or `ErrImagePull`

The images were not loaded into minikube. Run:

```powershell
.\k8s\build-local-images.ps1
```

Then restart the affected deployments:

```powershell
kubectl rollout restart deployment -n soa-local
```

### `apiserver: Stopped` / minikube won't start

Delete and recreate the cluster:

```powershell
minikube delete
minikube start --driver=docker --memory=6100 --cpus=2
```

### Pods stuck in `Pending`

Usually means minikube is out of resources. Check:

```powershell
kubectl describe pod -n soa-local <pod-name>
```

Look for `Insufficient memory` or `Insufficient cpu` in the Events section. If so, delete
and recreate minikube with more resources, or reduce the number of replicas.

### Pods keep restarting (`CrashLoopBackOff`)

Check the logs:

```powershell
kubectl logs -n soa-local <pod-name>
kubectl logs -n soa-local <pod-name> --previous
```

### Check logs for any service

```powershell
kubectl logs -n soa-local -l app=gateway-service --tail=50
kubectl logs -n soa-local -l app=auth-service --tail=50
# etc.
```
