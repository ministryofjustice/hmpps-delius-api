# 9. Follow GDS and HMPPS API Standards

Date: 2021-04-09

## Status

Accepted

## Context

The overall design of the API is in place for the initial use-cases
identified. The design decisions made have been based on following GDS
guidelines and also practices seen around the other HMPPS digital teams.

## Decision

We follow the API standards as defined by both GDS and HMPPS in the following areas:

### GDS Standards

We follow the [GDS API Technical Standards](https://www.gov.uk/guidance/gds-api-technical-and-data-standards)
for general API design guidelines. The guidelines are followed explicitly for:

* REST as an overall style
* HTTPS transport
* JSON media type
* ISO 8601 date/time fields
* UTF8 encoding
* User-level authentication/authorisation using OAuth2 (HMPPS-Auth)
  authorisation code grant type
* Client-level authorisation using OAuth2 (HMPPS-Auth) client credentials
  grant type
* Per-application client identification
* Use of JWT to encode authentication and authorisation
* Avoiding namespacing of the API URLs
* Using shallow resource hierarchies (less than three deep)
* Iteration of the API via non-breaking changes, backwards compatibility and
  versioning for significant changes
* Using path parameters for resource identifiers
* Using a coarse-grained API versioning scheme (e.g. /v1)
* Providing a test service
* Documenting the API using OpenAPI v3

### HMPPS Standards

We follow the [HMPPS Tech Team Standards](https://tech-docs.hmpps.service.justice.gov.uk/#readme) for more
specific API implementation details. The guidelines are followed explicitly for:

* Providing an application README
* Standard service endpoints (/health, /info etc.)

### Additional Local Decisions

* JSON keys are consistently camelCase
* Use of '409 Conflict' to indicate resource clashes

## Consequences

The practices we have adopted have enabled us the build the API quickly and in
a manner consistent with other government and HMPPS services.
