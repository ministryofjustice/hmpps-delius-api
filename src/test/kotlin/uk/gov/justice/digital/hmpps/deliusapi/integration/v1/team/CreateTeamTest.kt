package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.team

import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.TeamRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeAndProviderCodeOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("test-h2")
class CreateTeamTest : IntegrationTestBase() {

  @Autowired
  private lateinit var teamRepository: TeamRepository

  @Test
  fun `creating a new team`() {
    val request = Fake.validNewTeam()

    webTestClient.whenCreatingTeam(request)
      // Then it should return successfully
      .expectStatus().isCreated
      .expectBody()
      .shouldReturnCreatedTeam(request)
      // And it should save the entity to the database with the correct details
      .shouldSaveTeam(request)
  }

  protected fun WebTestClient.whenCreatingTeam(request: NewTeam): WebTestClient.ResponseSpec = this
    .post().uri("/v1/team")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()

  private fun WebTestClient.BodyContentSpec.shouldReturnCreatedTeam(request: NewTeam): WebTestClient.BodyContentSpec {
    jsonPath("$.code").value(Matchers.matchesRegex("C00\\d{3}"))
    jsonPath("$.description").value(Matchers.equalTo(request.description))
    jsonPath("$.provider").value(Matchers.equalTo(request.provider))
    jsonPath("$.ldu").value(Matchers.equalTo(request.ldu))
    jsonPath("$.unpaidWorkTeam").value(Matchers.equalTo(request.unpaidWorkTeam))
    jsonPath("$.type").value(Matchers.equalTo(request.type))
    return this
  }

  private fun WebTestClient.BodyContentSpec.shouldSaveTeam(request: NewTeam): WebTestClient.BodyContentSpec =
    this.jsonPath("$.code").value<String> {
      val savedTeam = teamRepository.findByCodeAndProviderCodeOrBadRequest(it, request.provider)
      Assertions.assertThat(savedTeam).describedAs("should save entity").isNotNull
      Assertions.assertThat(savedTeam.code).isEqualTo(it)
      Assertions.assertThat(savedTeam.description).isEqualTo(request.description)
      Assertions.assertThat(savedTeam.provider.code).isEqualTo(request.provider)
      Assertions.assertThat(savedTeam.localDeliveryUnit.code).isEqualTo(request.ldu)
      Assertions.assertThat(savedTeam.unpaidWorkTeam).isEqualTo(request.unpaidWorkTeam)
      Assertions.assertThat(savedTeam.teamType.code).isEqualTo(request.type)
      Assertions.assertThat(savedTeam.startDate).isEqualTo(LocalDate.now())
      Assertions.assertThat(savedTeam.createdDateTime)
        .isCloseTo(LocalDateTime.now(), Assertions.within(1, ChronoUnit.MINUTES))
    }
}
