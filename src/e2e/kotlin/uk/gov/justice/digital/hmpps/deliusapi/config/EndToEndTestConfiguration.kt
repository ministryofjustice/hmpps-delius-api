package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "e2e")
class EndToEndTestConfiguration(
  val url: String,
  val databaseAssert: Boolean,
  val oauth: OAuthConfiguration,
  val offenderCrn: String,
  val provider: String,
  val team: String,
  val staff: String,
  val contacts: ContactTestsConfiguration,
  val nsis: NsiTestsConfiguration,
  val staffs: StaffTestsConfiguration,
  val teams: TeamTestsConfiguration,
)
