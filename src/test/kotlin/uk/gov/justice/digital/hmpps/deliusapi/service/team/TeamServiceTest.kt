package uk.gov.justice.digital.hmpps.deliusapi.service.team

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
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
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.TeamMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TeamRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceTest {
  @Mock
  private lateinit var providerRepository: ProviderRepository

  @Mock
  private lateinit var teamRepository: TeamRepository

  @Mock
  private lateinit var mapper: TeamMapper

  @InjectMocks
  lateinit var subject: TeamService

  private lateinit var request: NewTeam

  private val provider = Fake.provider()

  private val team = Fake.team(provider)

  @Test
  fun `creating team`() {
    withRequest()
    havingRepositories()
    whenCallingCreateTeam()
    shouldSaveTeam()
  }

  @Test
  fun `attempting to create team with invalid provider`() {
    withRequest()
    havingRepositories(providerExists = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `attempting to create team with invalid cluster`() {
    withRequest(unknownCluster = true)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `attempting to create team with invalid team type`() {
    withRequest(unknownTeamType = true)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `attempting to create team with invalid ldu`() {
    withRequest(unknownLdu = true)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `attempting to create team with invalid code`() {
    withRequest(badCode = true)
    havingRepositories()
    shouldThrowBadRequest()
  }

  private fun withRequest(unknownCluster: Boolean = false, unknownTeamType: Boolean = false, unknownLdu: Boolean = false, badCode: Boolean = false) {
    request = Fake.teamMapper.toNew(team)

    if (unknownCluster)
      request = request.copy(cluster = "XYZ")

    if (unknownTeamType)
      request = request.copy(type = "XYZ")

    if (unknownLdu)
      request = request.copy(ldu = "XYZ")

    if (badCode)
      request = request.copy(code = request.code.reversed())
  }

  private fun havingRepositories(providerExists: Boolean = true) {
    if (providerExists) {
      whenever(providerRepository.findByCodeAndSelectableIsTrue(any())).thenReturn(provider)
    }

    whenever(teamRepository.findByCodeAndProviderCode(any(), any())).thenReturn(null)
    whenever(teamRepository.saveAndFlush(any())).thenReturn(team)
    whenever(mapper.toDto(any())).thenReturn(Fake.teamMapper.toDto(team))
  }

  private fun whenCallingCreateTeam() {
    val response = subject.create(request)
    val expectedResponse = Fake.teamMapper.toDto(team)

    assertThat(response).isEqualTo(expectedResponse)
  }

  private fun shouldThrowBadRequest() {
    assertThrows<BadRequestException> {
      subject.create(request)
    }
  }

  private fun shouldSaveTeam() {
    verify(teamRepository, times(1)).findByCodeAndProviderCode(request.code, provider.code)

    verify(teamRepository).saveAndFlush(
      ArgumentMatchers.argThat {
        !it.privateTeam &&
          it.code == request.code &&
          it.description == request.description &&
          it.localDeliveryUnit.code == request.ldu &&
          it.provider.code == request.provider &&
          it.teamType.code == request.type
      }
    )

    verifyNoMoreInteractions(teamRepository)
  }
}
