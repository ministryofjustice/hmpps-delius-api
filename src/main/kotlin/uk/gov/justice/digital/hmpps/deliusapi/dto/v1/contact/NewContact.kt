package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact

import uk.gov.justice.digital.hmpps.deliusapi.validation.AllowedValues
import uk.gov.justice.digital.hmpps.deliusapi.validation.Crn
import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.EnforcementActionCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroup
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroupType
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroups
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.OfficeLocationCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StaffCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.TeamCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.TimeRanges
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@TimeRanges
@FieldGroups
data class NewContact(
  @Crn
  val offenderCrn: String,

  @FieldGroup(FieldGroupType.EXCLUSIVE_ANY, "requirementId")
  @field:Positive
  val nsiId: Long? = null,

  @field:Positive
  val eventId: Long? = null,

  @field:Positive
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "eventId")
  val requirementId: Long? = null,

  @field:NotBlank
  @field:Size(max = 10)
  @AllowedValues("\${contacts.allowed-types}")
  val type: String,

  @NotBlankWhenProvided
  @field:Size(max = 10)
  override val outcome: String? = null,

  @EnforcementActionCode
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "outcome")
  override val enforcement: String? = null,

  @ProviderCode
  override val provider: String,

  @TeamCode
  override val team: String,

  @StaffCode
  override val staff: String,

  @OfficeLocationCode
  override val officeLocation: String? = null,

  override val date: LocalDate,

  @StartTime(name = "contact")
  override val startTime: LocalTime,

  @EndTime(name = "contact")
  override val endTime: LocalTime? = null,

  override val alert: Boolean = false,

  override val sensitive: Boolean = false,

  @field:Size(max = 4000)
  override val notes: String? = null,

  @field:Size(max = 200)
  override val description: String? = null,

  override val rarActivity: Boolean? = null
) : CreateOrUpdateContact
