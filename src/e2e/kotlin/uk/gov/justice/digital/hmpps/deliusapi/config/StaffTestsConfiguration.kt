package uk.gov.justice.digital.hmpps.deliusapi.config

open class StaffTestsConfiguration(
  open val withTeam: StaffTestConfiguration,
)

open class StaffTestConfiguration(
  open val lastName: String,
  open val firstName: String,
  open val provider: String,
  open val teams: List<String>,
)
