package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "e2e")
open class EndToEndTestConfiguration(
  open val url: String,
  open val databaseAssert: Boolean,
  open val oauth: OAuthConfiguration,
  open val offenderCrn: String,
  open val provider: String,
  open val team: String,
  open val staff: String,
  open val contacts: ContactTestsConfiguration,
  open val nsis: NsiTestsConfiguration,
  open val staffs: StaffTestsConfiguration,
  open val teams: TeamTestsConfiguration,
)
