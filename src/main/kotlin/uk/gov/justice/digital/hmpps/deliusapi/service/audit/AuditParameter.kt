package uk.gov.justice.digital.hmpps.deliusapi.service.audit

enum class AuditParameter(val code: String) {
  OFFENDER_ID("offenderId"),
  NSI_ID("nsiId"),
  CONTACT_ID("contactId"),
  PROVIDER_ID("probationAreaId")
}
