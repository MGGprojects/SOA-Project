$ErrorActionPreference = "Stop"

kubectl delete -f (Join-Path $PSScriptRoot "local-stack.yaml")
