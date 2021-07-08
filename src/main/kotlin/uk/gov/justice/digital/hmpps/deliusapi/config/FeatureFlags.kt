package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "features")
class FeatureFlags(
  val tokenVerification: Boolean = false,
  val nsiStatusHistory: Boolean = false,
)
