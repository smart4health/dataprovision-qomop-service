#!/bin/bash
## From a new terminal set the env vars to access vault as root
export VAULT_ADDR=http://127.0.0.1:8200
export VAULT_TOKEN=root

## Enable approle authentication and
vault auth enable approle

## Add policy for qomop to authenticate and pull secrets
vault policy write app - << EOF
path "secret/data/qomop/*" {
  capabilities = [ "read" ]
}
EOF

vault policy write deployment - << EOF
path "auth/approle/role/qomop/*" {
  capabilities = [ "read", "update" ]
}
EOF

## Add a role for qomop using this policy with a short TTL
vault write auth/approle/role/qomop token_policies=app token_ttl=20s

## Add the secrets so qomop doesn't crash on startup
vault kv put secret/qomop/rds-primary url="postgresql://localhost:5434/development?user=username&password=password"
vault kv put secret/qomop/rds-omop url="postgresql://localhost:5433/omop?user=username&password=password"

## Create a initial deployment token
vault token create -field token -policy=deployment
