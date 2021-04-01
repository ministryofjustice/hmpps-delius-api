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

    if (!request.code.startsWith(request.provider)) {
      throw BadRequestException("Team code must have provider '${provider.code}' prefix")
    }

    val cluster = provider.clusters.find { it.code == request.cluster }
      ?: throw BadRequestException("Cluster with id '${request.cluster}' does not exist on provider '${provider.code}'")

    val ldu = cluster.localDeliveryUnits.find { it.code == request.ldu }
      ?: throw BadRequestException("Local Delivery Unit with id '${request.ldu}' does not exist on cluster '${cluster.code}'")

    val teamType = provider.teamTypes.find { it.code == request.type }
      ?: throw BadRequestException("Team type with id '${request.type}' does not exist on provider '${provider.code}'")

    if (teamRepository.findByCodeAndProviderCode(request.code, provider.code) != null) {
      throw ConflictException("Team with id '${request.code}' already exists on provider '${provider.code}'")
    }

    val unallocatedStaff = Staff(
      code = request.code + UNALLOCATED_STAFF_CODE_SUFFIX,
      firstName = "Unallocated",
      middleName = "",
      lastName = "Staff",
      startDate = LocalDate.now(),
      privateStaff = provider.privateTrust,
      provider = provider
    )

    val team = Team(
      code = request.code,
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
}
