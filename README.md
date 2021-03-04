# HMPPS Delius API Service
![Integration Tests](https://codebuild.eu-west-2.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiUGhnUGFMQ0FXMlJZWTlOTk5zQ28zL3FkQnRsV0x5cnBNMEV4UTZHTE12QWRBUWlqU1gwNlpPM2RxU1RneVgySEVyMTBIVmJCZW1BMXB4RUlmME96S3NBPSIsIml2UGFyYW1ldGVyU3BlYyI6IjRPVWltcmppQnJhYVVzTHoiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=main)

## Overview

Service providing specific API services for the nDelius application. Developed
as a collaboration between MoJ Digital and Unilink.

Please raise any questions in the #delius-api Slack channel

## Purpose of the API Service 

The API service is intended to work as a entry-point for integration with
nDelius where it is important to apply nDelius application logic to API
operations. The API is closely tied to the nDelius data model and exposes some
internal implementation details. Some endpoints may require some
nDelius-specific knowledge to use such as entity identifiers (i.e. reference codes
where these exist but possibly ids where codes are not available). For this
reason we expect the API to be used as a backend service in combination with
other, higher-level, API services such as [Community API](https://github.com/ministryofjustice/community-api) 
where the endpoints provided by this service can be combined with others to
create more probation domain-specific APIs. 

Read-access to nDelius is currently provided by [Community API](https://github.com/ministryofjustice/community-api) 
and we will not look to replace these services as a priority, although that
may be added to the product roadmap as it develops.

### High-Level Architecture

![nDelius API](./doc/img/nDelius-API.png?raw=true)

## Initial Use-Cases 

### Writing Simple nDelius Contacts 

Writing data to the nDelius contact log, applying the nDelius application
logic for all associated actions triggered by adding the contact entry.

### Writing nDelius NSIs 

Writing NSIs of specific types to ensure contacts can be created and linked to
the correct container entities in nDelius 

## Key Technical Points

- Spring Boot service built mainly in Kotlin
- Intention is to reuse the logic in the nDelius Java application tier as far
  as possible 
- User and client authentication via HMPPS-Auth
- Requires access to the nDelius Oracle database 
- Deployed in the nDelius AWS environment

## Infrastructure 

### Code Repositories
- Application build and deployment pipeline definitions: https://github.com/ministryofjustice/hmpps-delius-api
- ECS service definition : https://github.com/ministryofjustice/hmpps-delius-core-terraform/tree/master/application/delius-api

### Image Repository
- Elastic Container Repository - https://gallery.ecr.aws/hmpps/delius-api
```shell
docker pull public.ecr.aws/hmpps/delius-api
```

### Deployment Pipelines 

#### Application Build, Test and Container Build 

- https://app.circleci.com/pipelines/github/ministryofjustice/hmpps-delius-api
- Each application branch is built and tested when pushed to GitHub
- A container is built and pushed to the application container repository on a 
  successful build of the `main` branch

### Technical Environments

- [Technical Environment Setup and Locations](./doc/architecture/technical-environments.md) 

## Building and Testing Locally 

``` sh
# Build and unit test the application 
./gradlew check 
```

## Development Setup

``` sh
# Install dependencies 
# JDK v11

# Check out the application code 
git clone git@github.com:ministryofjustice/hmpps-delius-api.git

# Build & run the application & dependencies in docker
docker-compose up --build --force-recreate

# Obtain an oauth token from the local HMPPS-Auth
AUTH_TOKEN=$( \
    curl --location \
         --request POST "http://localhost:9090/auth/oauth/token?grant_type=client_credentials" \
         --header "Authorization: Basic $(echo -n community-api-client:community-api-client | base64)" \
    | jq -r .access_token) 
    
# Make a request to the local Delius API
curl -v http://localhost:8081/health/ping --header "Authorization: Bearer $AUTH_TOKEN" | jq . 

# Run the application from the local filesystem sources 
./gradlew bootRun

```

## Integration Testing

### Development Database (In-Memory)

The service includes an in-memory data based for development purposes. This is
a lightweight but basic schema for use in initial development work but is not
representative of the production Delius database.

* H2 Web console - <http://localhost:8080/h2-console>
* JDBC URL: `jdbc:h2:file:./dev;MODE=Oracle;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9092`
* USER: `sa`
* PASSWORD: `password`

You can also connect to H2 remotely using the above JDBC URL - useful for Intellij 
database tools, and the extra code sense it adds to JPA.

The [src/main/resources/db](src/main/resources/db) directory contains the schema 
and data used for testing. On server startup, Flyway loads any new SQL files into
the local H2 database. Any changes to existing files will automatically clear down and
re-populate the database (`spring.flyway.clean-on-validation-error=true`).

### Oracle Database

The National Delius application uses an Oracle database, containing complex
PL/SQL code and triggers that can't be fully replicated by a H2 database
during dev/testing. 

A docker image is available in a private ECR repository with a snapshot of a
test Delius database, here: 

```
895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db
```

You can optionally run the application locally using the Oracle database image

```
docker-compose -f docker-compose.yml -f docker-compose.oracle.yml up --build --force-recreate
```

You will need access to the [pre-built Oracle image](oracledb/README.md) for this to work.

> :warning: The Oracle docker image is gigantic... 

to prevent stopped containers from filling up your disc make sure you run: 

`docker-compose -f docker-compose.yml -f docker-compose.oracle.yml down --remove-orphans`

Or just stop everything and run: `docker container prune`

See [oracledb/README.md](oracledb/README.md) for more details on accessing
this image, or building your own.  
