package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Team.getStaffOrBadRequest(staffCode: String): Staff =
  staff.find { it.staff.code == staffCode }?.staff
    ?: throw BadRequestException("Staff with officer code '$staffCode' does not exist in team '$code'")

fun Team.isUnallocated() = code.endsWith(UNALLOCATED_TEAM_PROVIDER_CODE_SUFFIX)

fun Team.getUnallocatedStaff(): Staff {
  val key = (if (code.length > 6) code.substring(0..5) else code) + UNALLOCATED_STAFF_CODE_SUFFIX
  return staff.find { it.staff.code == key }?.staff
    ?: throw RuntimeException("Team '$code' does not have an unallocated staff member")
}
