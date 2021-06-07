package uk.gov.justice.digital.hmpps.deliusapi.config

open class ContactTestsConfiguration(
  open val nsiOnly: ContactTestConfiguration,
  open val nsi: ContactTestConfiguration,
  open val rarNsi: ContactTestConfiguration,
  open val notUpdatable: ContactTestConfiguration,
  open val updatable: ContactTestConfiguration,
  open val event: ContactTestConfiguration,
  open val requirement: ContactTestConfiguration,
  open val rarRequirement: ContactTestConfiguration,
  open val enforcement: ContactTestConfiguration,
  open val appointment: ContactTestConfiguration,
  open val breachStart: ContactTestConfiguration,
  open val ftc: ContactTestConfiguration,
)

open class ContactTestConfiguration(
  open val type: String,
  open val outcome: String?,
  open val enforcement: String?,
  open val officeLocation: String?,
  open val eventId: Long?,
  open val requirementId: Long?,
)
