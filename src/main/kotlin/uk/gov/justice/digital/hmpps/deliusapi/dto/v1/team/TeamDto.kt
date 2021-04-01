package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamDto(
  val provider: String,
  val ldu: String,
  val type: String,
  val code: String,
  val description: String,
  val unpaidWorkTeam: Boolean,
  val startDate: LocalDate,
  val endDate: LocalDate?
)
