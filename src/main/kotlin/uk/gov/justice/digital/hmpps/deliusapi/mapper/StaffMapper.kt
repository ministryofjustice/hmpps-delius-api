package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StaffTeam

@Mapper
abstract class StaffMapper {
  @Mappings(
    Mapping(source = "provider.code", target = "provider"),
  )
  abstract fun toDto(staff: Staff): StaffDto

  abstract fun toNew(staff: StaffDto): NewStaff

  fun staffTeamToCode(value: StaffTeam): String {
    return value.team.code
  }

  companion object {
    val INSTANCE: StaffMapper = Mappers.getMapper(StaffMapper::class.java)
  }
}
