package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isUnallocated

@Mapper
interface NsiMapper {
  @Mappings(
    Mapping(source = "type.code", target = "type"),
    Mapping(source = "subType.code", target = "subType"),
    Mapping(source = "offender.crn", target = "offenderCrn"),
    Mapping(source = "event.id", target = "eventId"),
    Mapping(source = "requirement.id", target = "requirementId"),
    Mapping(source = "status.code", target = "status"),
    Mapping(source = "outcome.code", target = "outcome"),
    Mapping(source = "intendedProvider.code", target = "intendedProvider"),
    Mapping(target = "manager", expression = "java(toDto(src.getManagers().get(0)))"),
  )
  fun toDto(src: Nsi): NsiDto

  fun toNew(src: NsiDto): NewNsi

  @Mappings(
    Mapping(source = "provider.code", target = "provider"),
    Mapping(target = "team", expression = "java($CODE_OR_UNALLOCATED(src.getTeam()))"),
    Mapping(target = "staff", expression = "java($CODE_OR_UNALLOCATED(src.getStaff()))"),
  )
  fun toDto(src: NsiManager): NsiManagerDto

  fun toNew(src: NsiManagerDto): NewNsiManager

  companion object {
    private const val CODE_OR_UNALLOCATED = "NsiMapper.Companion.codeOrUnallocated"
    val INSTANCE = Mappers.getMapper(NsiMapper::class.java)
    fun codeOrUnallocated(staff: Staff?) = if (staff == null || staff.isUnallocated()) null else staff.code
    fun codeOrUnallocated(team: Team?) = if (team == null || team.isUnallocated()) null else team.code
  }
}
