package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact

import uk.gov.justice.digital.hmpps.deliusapi.validation.Crn
import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroup
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroupType
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class ReplaceContact(
  @Crn
  val offenderCrn: String,

  @NotBlankWhenProvided
  @field:Size(max = 10)
  val outcome: String,

  val date: LocalDate,

  @StartTime(name = "contact")
  val startTime: LocalTime,

  @EndTime(name = "contact")
  val endTime: LocalTime?,

  @field:Positive
  val eventId: Long? = null,

  @FieldGroup(FieldGroupType.EXCLUSIVE_ANY, "requirementId")
  @field:Positive
  val nsiId: Long? = null,

  @field:Positive
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "eventId")
  val requirementId: Long? = null,
)
