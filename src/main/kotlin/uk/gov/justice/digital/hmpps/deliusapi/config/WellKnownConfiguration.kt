package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "well-known")
class WellKnownConfiguration(
  val rescheduledAppointmentOutcomes: List<String> = emptyList()
)
