#!/usr/bin/sh

set -e

mkdir -p $HOME/.aws

cat - <<EOF > $HOME/.aws/config
[default]
role_arn = $(cat $ROLE_ARN_LOCATION)
credential_source = Ec2InstanceMetadata
EOF

CONNECTION_URL_SECRET=$(aws secretsmanager get-secret-value --secret-id $(cat $CONNECTION_URL_LOCATION))
export CONN=$(echo $CONNECTION_URL_SECRET | jq -r ".SecretString")