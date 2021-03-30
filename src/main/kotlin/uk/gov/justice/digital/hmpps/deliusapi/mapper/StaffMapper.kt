package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team

@Mapper
interface StaffMapper {
  @Mappings(
    Mapping(source = "provider.code", target = "provider"),
  )
  fun toDto(staff: Staff): StaffDto

  fun toNew(staff: StaffDto): NewStaff

  fun mapTeamToCode(value: Team): String = value.code

  companion object {
    val INSTANCE: StaffMapper = Mappers.getMapper(StaffMapper::class.java)
  }
}
