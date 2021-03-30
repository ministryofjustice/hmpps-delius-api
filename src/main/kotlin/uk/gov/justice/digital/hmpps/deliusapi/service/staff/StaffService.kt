package uk.gov.justice.digital.hmpps.deliusapi.service.staff

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.StaffMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.StaffRepository
import java.time.LocalDate

@Service
class StaffService(
  private val staffRepository: StaffRepository,
  private val providerRepository: ProviderRepository,
  private val mapper: StaffMapper
) {
  @Transactional
  fun createStaff(request: NewStaff): StaffDto {
    val staffCode = providerRepository.getNextStaffCode(request.provider)
    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider '${request.provider}' does not exist")

    val teams = request.teams?.map { teamCode ->
      provider.teams?.find { team -> team.code == teamCode }
        ?: throw BadRequestException("Team with id '$teamCode' does not exist on provider '${provider.code}'")
    }

    val staff = Staff(
      code = staffCode,
      startDate = LocalDate.now(),
      lastName = request.lastName,
      middleName = null,
      firstName = request.firstName,
      privateStaff = provider.privateTrust,
      provider = provider,
      teams = teams
    )

    return mapper.toDto(staffRepository.saveAndFlush(staff))
  }
}
