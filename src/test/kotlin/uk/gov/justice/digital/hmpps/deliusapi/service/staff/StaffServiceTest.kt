package uk.gov.justice.digital.hmpps.deliusapi.service.staff

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.StaffMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StaffServiceTest {

  @Mock
  private lateinit var staffRepository: StaffRepository

  @Mock
  private lateinit var providerRepository: ProviderRepository

  @Mock
  private lateinit var mapper: StaffMapper

  @InjectMocks
  lateinit var subject: StaffService

  private lateinit var request: NewStaff

  private val provider = Fake.provider()

  private val staff = Fake.staff(provider)

  @Test
  fun `creating staff member`() {
    withRequest()
    havingRepositories()
    whenCallingCreateStaff()
    shouldSaveStaff()
  }

  @Test
  fun `attempting to create staff with invalid provider`() {
    withRequest()
    havingRepositories(providerExists = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `attempting to create staff with invalid team`() {
    withRequest(invalidTeam = true)
    shouldThrowBadRequest()
  }

  private fun withRequest(invalidTeam: Boolean = false) {

    if (!invalidTeam) {
      staff.apply {
        staff.teams.clear()
        this.addTeam(provider!!.teams.first())
      }
    }

    request = Fake.staffMapper.toNew(Fake.staffMapper.toDto(staff))
  }

  private fun havingRepositories(providerExists: Boolean = true) {
    if (providerExists) {
      whenever(providerRepository.findByCodeAndSelectableIsTrue(any())).thenReturn(provider)
    }
    whenever(providerRepository.getNextStaffCode(any())).thenReturn("ABC001")
    whenever(staffRepository.saveAndFlush(any())).thenReturn(staff)
    whenever(mapper.toDto(any())).thenReturn(Fake.staffMapper.toDto(staff))
  }

  private fun whenCallingCreateStaff() {
    val response = subject.createStaff(request)
    val expectedResponse = Fake.staffMapper.toDto(staff)

    assertThat(response).isEqualTo(expectedResponse)
  }

  private fun shouldThrowBadRequest() {
    assertThrows<BadRequestException> {
      subject.createStaff(request)
    }
  }

  private fun shouldSaveStaff() {
    verify(staffRepository).saveAndFlush(
      ArgumentMatchers.argThat {
        !it.privateStaff &&
          it.code == "ABC001" &&
          it.firstName == request.firstName &&
          it.lastName == request.lastName &&
          it.provider == provider
      }
    )

    verifyNoMoreInteractions(staffRepository)
  }
}
