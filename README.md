# Smart4Health Data-provision QOMOP Service

## Acknowledgements

<img src="./img/eu.jpg" align="left" alt="European Flag" width="60">

This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 826117.

## About

Note: QOMOP stands for  "Query service for OMOP" (QOMOP)

This service has its place within the data-provision pipeline for Smart4Health. It has two modules Harmonizer and
Tracer.

The Harmonizer takes a FHIR Coding (system and code), checks if it is part of a standard vocabulary and harmonizes it to
one if this is not the case already. This module and its endpoint are used by
the [Data-provision Deident Service](https://github.com/smart4health/dataprovision-deident-service).

The Tracer takes a FHIR coding and a Smart4Health internally defined "Data Category" and checks if this coding is part
of it. This module and its endpoint are used by the [MyScience App](https://github.com/smart4health/my-science-app).

## Modules

### Harmonizer: map FHIR codings to standardized ones

For the data provision pipeline the system-code pairs (FHIR `Coding`) need to be mapped to standardized vocabularies.
The harmonization endpoint takes a Coding as input and returns the result of
from `com.healthmetrix.qomop.harmonizer.controllers.HarmonizationController.HarmonizationResponse`.

```shell
curl --location --request POST 'http://localhost:6060/v1/harmonizer/coding' \
--header 'Content-Type: application/json' \
--data-raw '{
    "system": "http://www.whocc.no/atc",
    "code": "N02BE01"
}'
```

### Tracer: Look up FHIR coding (OMOP Concept) items for being contained in a specific data category

The citizen defines in the MyScience app which FHIR data should be committed through the data provision pipeline. Each
of
those decisions is persisted in its own Data Category. The app calls the qomop service for each category to determine if
the Resource (more specifically the coding) is contained in the category.

```shell
curl --location --request POST 'http://localhost:6060/v1/tracer/data-category/5' \
--header 'Content-Type: application/json' \
--data-raw '{
    "code": "82046009",
    "system": "http://snomed.info/sct"
}'
```

## Swagger documentation

Accessible at `{host}/docs-ui.html`

## Manual harmonization feedback loop

Failed FHIR Coding -> OMOP Concept mappings can be exported, processed with Usagi and imported to this service. For more
information check the designated documentation in the [docs](docs) directory.

1. Export:

```
curl --location --request GET 'localhost:6060/v1/harmonization-config/coding/export' -o export.csv
```

2. Run through OMOP, export using _saveAs_
3. Import:

```
curl --location --request POST 'localhost:6060/v1/harmonization-config/coding/import' --form 'file=@"harmonizer/src/test/resources/csv/mappings-to-upload.csv"'
```

## Local deployment of the data sources

The application has two DataSources. The primary one and OMOP cdm. Both have to be started to deploy locally:

`./gradlew bootRun`

### datasource-primary

The port is set to 5434 so qomop and deident can run on the same machine.

```shell
docker run --rm -it --name postgres-qomop -e POSTGRES_PASSWORD=password -e POSTGRES_USER=username -e POSTGRES_DB=development -p 5434:5432 postgres
```

To inspect the database, in a new terminal, run

```shell script
docker exec -it postgres-qomop psql -h localhost -U username
```

From there, you can run queries against the tables, or see available tables with `\d`

### datasource-omop (OMOP CDM)

Set up the OMOP CDM based on the documentation by OHDSI: https://github.com/OHDSI/CommonDataModel

Note that as configured in that repo's docker-compose it should accept connections on port 5433,
not the default 5432 to not mess with the primary DataSource.

Check after the job that the database name (omop) and the schema (cdm) is created and that all tables (e.g. concept) are
contained in it. Otherwise, rename those manually.

## Hashicorp Vault local deployment

To run Vault locally and let qomop use approle authentication using sample secrets, proceed the following:

1. Start Vault dev server locally (dev mode skips unsealing and some defaults):

```shell
docker run --rm -p 8200:8200 --cap-add=IPC_LOCK --name=vault-dev -e 'VAULT_DEV_ROOT_TOKEN_ID=root' vault
```

2. Run this custom set up script for the initial token, policies, roles and secrets. Also starts the service which will
   use the initial token to renew the approle lease every 20 seconds and using full pull mode to fetch the role-id and
   secret-id:

```shell
SPRING_CLOUD_VAULT_TOKEN=$(sh vault_local.sh | tail -1) SPRING_PROFILES_ACTIVE=secrets-vault ./gradlew bootRun
```


## Code Stats
Generated with [tokei](https://github.com/XAMPPRocky/tokei)

```
===============================================================================
 Language            Files        Lines         Code     Comments       Blanks
===============================================================================
 Batch                   1           91           70            0           21
 Dockerfile              1           14           14            0            0
 JSON                    9          412          411            0            1
 Kotlin                 96         4229         3500          172          557
 Shell                   4          289          131          120           38
 SQL                     1           11           11            0            0
 Plain Text              1           13            0           11            2
 TOML                    1           99           75            1           23
 XML                     1           19           19            0            0
 YAML                    2          138          110            6           22
-------------------------------------------------------------------------------
 Markdown                3          272            0          184           88
 |- Shell                1           17           17            0            0
 |- YAML                 1            8            8            0            0
 (Total)                            297           25          184           88
===============================================================================
 Total                 120         5587         4341          494          752
===============================================================================
```