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

## Infrastructure and CI/CD Processes

* Each application branch is built and tested when pushed to GitHub. A container
  is built and pushed to the application container repository on a successful
  build of the `main` branch.
* A successful merge to the `main` branch triggers a deployment to the
  [Test](./doc/architecture/technical-environments.md#test) environment
* End-to-end tests are run daily against the
  [Test](./doc/architecture/technical-environments.md#test) environment which
  includes the Delius service and database.

| Purpose                                     | Location                                                                                                                           | Authentication                      |
|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| Application Build and Deployment Definition | [CircleCI Pipeline Definition](https://github.com/ministryofjustice/hmpps-delius-api/tree/main/.circleci)                          | MoJ SSO / GitHub                    |
| Application Build, Test and Deploy          | [CircleCI](https://app.circleci.com/pipelines/github/ministryofjustice/hmpps-delius-api)                                           | MoJ SSO / GitHub                    |
| ECS Runtime Definition                      | [ECS service definition](https://github.com/ministryofjustice/hmpps-delius-core-terraform/tree/master/application/delius-api)      | MoJ SSO / GitHub                    |
| Container Image Repository                  | [Elastic Container Repository](https://gallery.ecr.aws/hmpps/delius-api)                                                           | AWS HMPPS-Probation / MoJDevelopers |
| End-to-End Test Definition                  | [AWS CodeBuild](https://github.com/ministryofjustice/hmpps-delius-pipelines/blob/master/components/delius-core/test-delius-api.tf) | MoJ SSO / GitHub                    |

## FAQ

### How do I get started developing the API?

Read the documentation around [Local Development Setup](./doc/development.md)
to help with setting up a local environment including dependencies.

### How is the API tested?

Information on development and end-to-end testing locally and in a
representative environment is in the [Testing Documentation](./doc/testing.md)

### How and where is the API service deployed?

Read the information on the [Technical Environment Setup and Locations](./doc/architecture/technical-environments.md)
to understand how the API service is made available
