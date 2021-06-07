package uk.gov.justice.digital.hmpps.deliusapi.config

open class NsiTestsConfiguration(
  open val active: NsiTestConfiguration,
  open val terminated: NsiTestConfiguration,
  open val refer: NsiTestConfiguration,
  open val rar: NsiTestConfiguration,
)

open class NsiTestConfiguration(
  open val type: String,
  open val subType: String?,
  open val status: String,
  open val eventId: Long?,
  open val requirementId: Long?,
  open val outcome: String?,
  open val length: Long?,
)
