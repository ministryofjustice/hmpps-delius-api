package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact

import java.time.LocalDate
import java.time.LocalTime

interface CreateOrUpdateContact {
  val outcome: String?
  val enforcement: String?
  val provider: String
  val team: String
  val staff: String
  val officeLocation: String?
  val date: LocalDate
  val startTime: LocalTime
  val endTime: LocalTime?
  val alert: Boolean
  val sensitive: Boolean
  val notes: String?
  val description: String?
  val rarActivity: Boolean?
}
