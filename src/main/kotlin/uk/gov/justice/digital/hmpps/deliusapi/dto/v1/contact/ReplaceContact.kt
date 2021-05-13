package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact

import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.Size

data class ReplaceContact(
  @NotBlankWhenProvided
  @field:Size(max = 10)
  val outcome: String,

  val date: LocalDate,

  @StartTime(name = "contact")
  val startTime: LocalTime,

  @EndTime(name = "contact")
  val endTime: LocalTime?,
)
