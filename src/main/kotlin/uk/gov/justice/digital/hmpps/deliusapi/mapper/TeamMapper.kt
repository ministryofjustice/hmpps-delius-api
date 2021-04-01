package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.TeamDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team

@Mapper
interface TeamMapper {
  @Mappings(
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "localDeliveryUnit.code", target = "ldu"),
    Mapping(source = "teamType.code", target = "type"),
  )
  fun toDto(team: Team): TeamDto

  @Mappings(
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "teamType.code", target = "type"),
    Mapping(source = "localDeliveryUnit.code", target = "ldu"),
    Mapping(source = "localDeliveryUnit.cluster.code", target = "cluster"),
  )
  fun toNew(team: Team): NewTeam

  companion object {
    val INSTANCE: TeamMapper = Mappers.getMapper(TeamMapper::class.java)
  }
}
