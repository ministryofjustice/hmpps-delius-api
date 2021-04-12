# Delius API Testing Strategy

API endpoints should have unit and integration test coverage for development
purposes and end-to-end coverage for inclusion in ongoing CI smoke tests.

## Unit Testing

Unit tests are written in Kotlin using JUnit 5. Each class should have
associated unit tests covering business logic.

## Integration Testing

Integration tests are written in Kotlin using JUnit 5. Each class should have
associated integration tests covering API functionality.

## End-to-End Testing

End-to-end tests are provided via the `e2e` gradle task:

```bash
./gradlew e2e
```

Process:

1. **generateApiSpec** API started and OAPI 3.0 document generated & saved in `build/generated`.
2. **openApiGenerate** OAPI document used to generate a kotlin API client in `src/generated`.
3. **e2e** JUnit tests written in Kotlin against the generated client.

The `e2e` task actually spins up the delius API Spring boot application but with the API disabled.
This is for configuration & DI in order to connect to the database for test setup & teardown.

Environment configuration is handled by Spring configuration.
The `e2e` profile is always loaded so defaults can be found: `src/e2e/resources/application-e2e.yml`.
