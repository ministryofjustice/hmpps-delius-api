package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff

import java.time.LocalDate

data class StaffDto(
  val code: String,
  var startDate: LocalDate,
  val lastName: String,
  val firstName: String,
  val privateStaff: Boolean,
  val provider: String,
  val teams: MutableList<String>,
)
