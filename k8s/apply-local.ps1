$ErrorActionPreference = "Stop"

kubectl apply -f (Join-Path $PSScriptRoot "local-stack.yaml")
