# rds-psql-util

A utility image to parse terraform output, pull aws secrets,
and run psql commands postgres connection information.

The aws-cli triples the image size, to around 300 megabytes.

There is no CI set up, it's too small to bother.  After modifying,
build and push to `latest`.

## Build

```
$ docker build -t healthmetrixgmbh/rds-psql-util:latest .
```

## Push

```
$ docker push healthmetrixgmbh/rds-psql-util:latest
```

## Usage

Should be used with a list of commands, not as a drone plugin.

There is a helper to load the admin connection url, usage looks like:

```yaml
- name: inspect permissions
  image: healthmetrixgmbh/rds-psql-util:latest
  environment:
    ROLE_ARN_LOCATION: deployment/terraform_omop/.deployment_role_arn
    CONNECTION_URL_LOCATION: deployment/terraform_omop/.admin_database_connection_url_location
  commands:
    - . /load-conn.sh
    - psql $CONN -c '\du'
```

where the files at the locations are generally the output of `terraform output -raw`