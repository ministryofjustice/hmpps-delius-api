package uk.gov.justice.digital.hmpps.deliusapi.service.team

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.TeamDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StaffTeam
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.ConflictException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.TeamMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TeamRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeAndSelectableIsTrueOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderRequestAuthority
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.UNALLOCATED_STAFF_CODE_SUFFIX
import java.time.LocalDate

@Service
class TeamService(
  private val teamRepository: TeamRepository,
  private val providerRepository: ProviderRepository,
  private val mapper: TeamMapper
) {
  @Transactional
  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.ADD_TEAM)
  fun create(request: NewTeam): TeamDto {
    val provider = providerRepository.findByCodeAndSelectableIsTrueOrBadRequest(request.provider)

    val audit = AuditContext.get(AuditableInteraction.ADD_TEAM)
    audit.providerId = provider.id

    val newTeamCode = getNextAvailableTeamCode(provider.code)

    val cluster = provider.clusters.find { it.code == request.cluster }
      ?: throw BadRequestException("Cluster with id '${request.cluster}' does not exist on provider '${provider.code}'")

    val ldu = cluster.localDeliveryUnits.find { it.code == request.ldu }
      ?: throw BadRequestException("Local Delivery Unit with id '${request.ldu}' does not exist on cluster '${cluster.code}'")

    val teamType = provider.teamTypes.find { it.code == request.type }
      ?: throw BadRequestException("Team type with id '${request.type}' does not exist on provider '${provider.code}'")

    if (teamRepository.findByCodeAndProviderCode(newTeamCode, provider.code) != null) {
      throw ConflictException("Team with code '$newTeamCode' already exists on provider '${provider.code}'")
    }

    val unallocatedStaff = Staff(
      code = newTeamCode + UNALLOCATED_STAFF_CODE_SUFFIX,
      firstName = "Unallocated",
      middleName = "",
      lastName = "Staff",
      startDate = LocalDate.now(),
      privateStaff = provider.privateTrust,
      provider = provider
    )

    val team = Team(
      code = newTeamCode,
      description = request.description,
      provider = provider,
      localDeliveryUnit = ldu,
      teamType = teamType,
      privateTeam = provider.privateTrust,
      unpaidWorkTeam = request.unpaidWorkTeam,
      startDate = LocalDate.now(),
    )

    team.staff.add(StaffTeam(staff = unallocatedStaff, team = team))

    return mapper.toDto(teamRepository.saveAndFlush(team))
  }

  fun getNextAvailableTeamCode(providerCode: String): String {
    val existingHighest = teamRepository.findByProviderCode(providerCode)
      .filter { it.code.matches("$providerCode\\d{3}".toRegex()) }
      .map { Integer.parseInt(it.code.substring(3, 6)) }
      .maxOrNull() ?: 0

    return "$providerCode${(existingHighest + 1).toString().padStart(3, '0')}"
  }
}
