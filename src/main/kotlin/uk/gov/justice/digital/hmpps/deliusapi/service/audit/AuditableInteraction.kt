package uk.gov.justice.digital.hmpps.deliusapi.service.audit

enum class AuditableInteraction(val code: String) {
  ADD_CONTACT("CLBI003"),
  UPDATE_CONTACT("CLBI007"),
  ADMINISTER_NSI("NIBI009"),
  ADD_TEAM("SPBI017"),
  CREATE_STAFF("SPBI028"),
}
