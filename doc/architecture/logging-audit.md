# Logging and Audit Strategy

## Audit

### General Strategy

The Delius API is capable of ensuring audit entries are made for all
operations within Delius that require this. Audit records are created
by adding to the audit table of the Delius database. Within the service
operations are defined as needing audit records by annotating service methods
as 'auditable' and supplying the correct interaction type code.

``` kotlin

@Auditable(AuditableInteraction.UPDATE_CONTACT)
fun updateContact(id: Long, request: UpdateContact): ContactDto {

```

### Audit Criteria

The audit process is seen as critical and if an audit record cannot be
created for the interaction the entire operation is rejected. The audit record
includes the Delius user as supplied by the HMPPS-Auth token. The user
supplied by the authentication token may be either an end user or a system
client user. For system client users we will audit the operation has been made
by that client only, not the specific user. To determine the specific user we
rely on linking the operation to the associated operation in the client
system's audit trail.

## Logging

### General Strategy

The Delius API writes log entries only in specific cases:

1. All exceptions are logged
2. Failed audit operations are logged

Logs are written to two log appenders which direct the log entries to specific
destinations:

1. The system console, which routes to CloudWatch in an AWS environment
2. Azure Application Insights

The two log destinations are accessible to authenticated users only and are
read-only meaning logs cannot be tampered with. Using multiple services to
capture logs means a record of activity is held in multiple places in case of
service availability problems with either. Logs are relied on for problem
investigation and providing an aggregate overview of service operability (e.g.
request/response timings, response codes etc.) as such and are seen as a
critical element of the service.
