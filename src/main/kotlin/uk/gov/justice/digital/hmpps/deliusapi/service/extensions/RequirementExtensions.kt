package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.RequirementTypeCategory.Companion.RAR_REQUIREMENT_TYPE_CATEGORY_CODE
import java.time.LocalDate

fun Requirement.isRehabilitationActivityRequirement() = typeCategory?.code == RAR_REQUIREMENT_TYPE_CATEGORY_CODE

fun Requirement.isTerminated(referenceDate: LocalDate): Boolean {
  val terminationDate = terminationDate
  return terminationDate != null && terminationDate <= referenceDate
}
