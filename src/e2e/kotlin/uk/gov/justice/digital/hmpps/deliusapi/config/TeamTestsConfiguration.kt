package uk.gov.justice.digital.hmpps.deliusapi.config

open class TeamTestsConfiguration(
  open val default: TeamTestConfiguration,
)

open class TeamTestConfiguration(
  open val cluster: String,
  open val ldu: String,
  open val provider: String,
  open val type: String,
  open val description: String,
  open val unpaidWorkTeam: Boolean,
)
