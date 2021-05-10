# Delius API Development

## Dependencies
* [JDK 11](https://openjdk.java.net/projects/jdk/11/)

## Source
```shell
# Check out the application code
git clone git@github.com:ministryofjustice/hmpps-delius-api.git

# or, pull the latest docker image
docker pull public.ecr.aws/hmpps/delius-api
```

## Run
### Using Gradle
```shell
# Build and unit test the application
./gradlew check

# Run the standalone Spring Boot application
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### Using Docker
```shell
# Build & run the application & dependencies in docker
docker-compose up --build --force-recreate
```

### Using IntelliJ
1. *Import* the project (File > New > Project from Version Control...)
2. Run the *HmppsAuth - dev* configuration
3. Run the *HmppsDeliusApi - dev* configuration

This will start up hmpps-auth in a Docker container, but runs the Spring Boot app
separately to allow better integration with IntelliJ dev tooling.

## Connect

For testing, use the`delius-api-client` client with secret: `delius-api-client`.
Client details are created here: [V900_1__delius_api_oauth.sql](/src/main/resources/db/auth/V900_1__delius_api_oauth.sql).

```shell
# Obtain an OAuth token from the local HMPPS-Auth container
AUTH_TOKEN=$(curl --request POST --user delius-api-client:delius-api-client \
             "http://localhost:9090/auth/oauth/token?grant_type=client_credentials" \
            | jq -r .access_token)

# Make a request to the local Delius API
curl -v http://localhost:8080/health/ping --header "Authorization: Bearer $AUTH_TOKEN" | jq .
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

The [src/main/resources/db](../src/main/resources/db) directory contains the schema
and data used for testing. On server startup, Flyway loads any new SQL files into
the local H2 database. Any changes to existing files will automatically clear down and
re-populate the database (`spring.flyway.clean-on-validation-error=true`).

### Docker Compose

There is a Docker Compose definition file which defines an integrated
development environment for the API. This comprises of containers for the
Delius API and the services the API depends on at runtime:

* OAuth2 Authentication and Authorisation: [HMPPS-Auth]()
* JWT Token Verification: [Token Verification Service]()

It is possible to start the dependencies only using this command:

```sh
docker-compose -f docker-compose.yml up oauth token-verification-api
```

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

You will need access to the [Pre-built Delius Oracle image](../oracledb/README.md) for this to work.

#### Usage Considerations

> :warning: The Oracle docker image is large (> 20G) and may cause problems
> with you local system if you do not take this into account

To prevent stopped containers from filling up your disc when you have finished
using the database container you can run:

`docker-compose -f docker-compose.yml -f docker-compose.oracle.yml down --remove-orphans`

As a last resort stop everything and remove all stopped containers using: `docker container prune`

See [Delius Oracle DB Container](../oracledb/README.md) for more details on accessing
this image, or building your own.
