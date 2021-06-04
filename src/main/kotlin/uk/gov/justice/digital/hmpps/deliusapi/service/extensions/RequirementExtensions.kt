package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import java.time.LocalDate

fun Requirement.isRehabilitationActivityRequirement() = typeCategory?.code == "F"

fun Requirement.isTerminated(referenceDate: LocalDate): Boolean {
  val terminationDate = terminationDate
  return terminationDate != null && terminationDate <= referenceDate
}
