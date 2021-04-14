package uk.gov.justice.digital.hmpps.deliusapi.v1.team

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.client.model.TeamDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.TeamTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newTeam
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.repository.TeamRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate

class CreateTeamTest @Autowired constructor(
  private val repository: TeamRepository,
) :
  EndToEndTest() {
  private lateinit var request: NewTeam
  private lateinit var response: TeamDto
  private lateinit var created: Team

  @Test
  @Transactional
  @Commit
  fun `Creating Team`() {
    request = configuration.newTeam(TeamTestsConfiguration::default)
    whenCreatingTeam()
    shouldReturnExpectedTeam()
    shouldSaveTeam()
    removeInsertedRows()
  }

  private fun whenCreatingTeam() {
    response = teamV1.safely { it.create(request) }
  }

  private fun shouldReturnExpectedTeam() {
    assertThat(response)
      .hasFieldOrProperty("code")
      .hasProperty(TeamDto::description, request.description)
      .hasProperty(TeamDto::ldu, request.ldu)
      .hasProperty(TeamDto::provider, request.provider)
      .hasProperty(TeamDto::startDate, LocalDate.now())
      .hasProperty(TeamDto::type, request.type)
      .hasProperty(TeamDto::unpaidWorkTeam, request.unpaidWorkTeam)

    assertThat(response.code).startsWith(request.provider)
    assertThat(response.startDate).isEqualTo(LocalDate.now())
  }

  private fun shouldSaveTeam() {
    if (!databaseAssertEnabled()) {
      return
    }

    created = repository.findByCodeAndProviderCode(response.code, response.provider)
      ?: throw RuntimeException("Team with code = '${response.code}' does not exist in the database")

    assertThat(created)
      .hasFieldOrPropertyWithValue("code", response.code)
      .hasFieldOrPropertyWithValue("localDeliveryUnit.code", request.ldu)
      .hasFieldOrPropertyWithValue("provider.code", request.provider)
      .hasFieldOrPropertyWithValue("teamType.code", request.type)
      .hasProperty(Team::description, request.description)
      .hasProperty(Team::privateTeam, true)
      .hasProperty(Team::startDate, LocalDate.now())
      .hasProperty(Team::unpaidWorkTeam, request.unpaidWorkTeam)
      .hasNoNullFieldsOrPropertiesExcept("endDate")
      .extracting("officeLocations").asList().isEmpty()

    assertThat(created.staff)
      .hasSize(1)
      .anyMatch { it.staff.code == created.code + "U" }
  }

  private fun removeInsertedRows() {
    if (databaseAssertEnabled()) {
      repository.delete(created)
    }
  }
}
