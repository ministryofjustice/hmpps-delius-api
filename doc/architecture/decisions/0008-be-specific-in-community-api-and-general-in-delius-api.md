# 8. Be Specific in Community API and General in Delius API

Date: 2021-03-02

## Status

Accepted

## Context

Digital Studio teams use and develop [Community API](https://github.com/ministryofjustice/community-api)
when building services that need to integrate with Delius data. This is a
well understood method of data exchange with the Delius database and gives the
Studio teams flexibility and control over how they model and access Delius
data. There are a number of cases where this interaction is complex and
requires more than simple database-level access, including applying access
restrictions and executing logic to decide on appropriate actions (e.g when
reallocating an offender manager). We are introducing the Delius API into the
application environment as a way of better controlling the interaction with
Delius and as a way of extending the logical boundary of Delius to provide
this type of complex operation as an API service.

The Delius API, by definition, provides access to the Delius domain model and
the endpoints it exposes are in the language of the Delius domain. Use of the
Delius domain model can be difficult as it often requires knowledge of the
internal structures, dependencies and data-level validations and restrictions
within Delius.

We would like to enable the Studio teams to model their interactions with
Delius in the language of their own services, whilst also reducing the need to
understand and re-implement Delius-specific logic and access control in
Community API.

## Decision

The Delius API will provide access to Delius services in the language of the
Delius domain. This may include execution of Delius logic, application of
access control and strong data validation on both a data and logical level
(i.e. fields are in the correct form, entities are consistent, and dependent
values make sense). These API endpoints are generic and flexible and as a
result effective use will require knowledge of Delius. To mitigate this we
will use Community API to interact with the Delius API in specific ways,
modelling the interaction via higher-level domain concepts (e.g. an
Intervention Referral) rather than the lower level Delius implementation (e.g.
an NSI) and reducing the surface area of Delius knowledge needed to achieve
specific goals.

## Consequences

By using the two API services together we maintain the Studio's existing, well
understood method of development when interaction with Delius is needed.
The main use case of the Community API integration is providing read-access to
Delius data and introducing the Delius API will not require existing services
to migrate away from this. We can continue to use the Community API-supplied
Delius integration as needed, however, the Delius API does give us a place
to implement Delius services that will back the Community API endpoints where
the Community API implementation is missing Delius functionality (e.g.
post-processing logic or validation). These can be identified and developed
over time and in priority order.

Community API gives us a place to model and encapsulate the complexity of
the Delius API usage, enabling Studio teams to interact with Delius without
needing deep knowledge of implementation details. This may mean encoding
specific Delius reference data values into Community API endpoints as static
configuration or constant variables.

Introduction of an extra service into the interaction chain may bring some
difficulties, such as making it more difficult to develop, test and debug
applications. Authentication is also made more complex as identifying the
user of the service requires passing on the authentication and identity
details through a chain of service calls.

It may be difficult to fully define and socialise boundary between Community
API and Delius API, particularly in the early stages. We are building the
Delius API initially to support the tightly scoped use case of Interventions
and development is continuing across the Studio on other projects that require
Delius interaction. The boundaries and appropriate use-cases of both services
should become clearer as the Delius API provides more services and takes on
more of the logical heavy-lifting needed when interacting with Delius, making
these activities easier for development teams.
