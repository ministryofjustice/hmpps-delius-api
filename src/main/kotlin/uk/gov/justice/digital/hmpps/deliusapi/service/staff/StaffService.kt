package uk.gov.justice.digital.hmpps.deliusapi.service.staff

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.StaffMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderRequestAuthority
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import java.time.LocalDate

@Service
class StaffService(
  private val staffRepository: StaffRepository,
  private val providerRepository: ProviderRepository,
  private val mapper: StaffMapper
) {
  @Transactional
  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.CREATE_STAFF)
  fun createStaff(request: NewStaff): StaffDto {
    val staffCode = providerRepository.getNextStaffCode(request.provider)
    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider '${request.provider}' does not exist")

    val audit = AuditContext.get(AuditableInteraction.CREATE_STAFF)
    audit.providerId = provider.id

    val staff = Staff(
      code = staffCode,
      startDate = LocalDate.now(),
      lastName = request.lastName,
      middleName = null,
      firstName = request.firstName,
      privateStaff = provider.privateTrust,
      provider = provider
    )

    request.teams.map { teamCode ->
      provider.teams.find { team -> team.code == teamCode }
        ?: throw BadRequestException("Team with id '$teamCode' does not exist on provider '${provider.code}'")
    }.map(staff::addTeam)

    return mapper.toDto(staffRepository.saveAndFlush(staff))
  }
}
