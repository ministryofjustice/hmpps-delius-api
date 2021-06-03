package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact

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
import javax.validation.constraints.Size

@TimeRanges
@FieldGroups
data class UpdateContact(
  @NotBlankWhenProvided
  @field:Size(max = 10)
  override val outcome: String?,

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
  override val officeLocation: String?,

  override val date: LocalDate,

  @StartTime(name = "contact")
  override val startTime: LocalTime,

  @EndTime(name = "contact")
  override val endTime: LocalTime?,

  override val alert: Boolean,

  override val sensitive: Boolean,

  @field:Size(max = 4000)
  override val notes: String?,

  @field:Size(max = 200)
  override val description: String?,

  override val rarActivity: Boolean?
) : CreateOrUpdateContact
