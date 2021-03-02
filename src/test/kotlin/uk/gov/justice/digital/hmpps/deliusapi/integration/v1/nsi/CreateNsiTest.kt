package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.nsi

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.comparingDateTimesToNearestSecond
import uk.gov.justice.digital.hmpps.deliusapi.validation.ValidationTestCase
import uk.gov.justice.digital.hmpps.deliusapi.validation.ValidationTestCaseBuilder
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test-h2")
class CreateNsiTest @Autowired constructor (
  private val nsiRepository: NsiRepository,
  private val contactRepository: ContactRepository,
) : IntegrationTestBase() {

  companion object {
    private fun validNsiFactory() = Fake.newNsi().copy(
      type = "KSS021",
      subType = "KSS026",
      status = "SLI01",
      statusDate = LocalDateTime.now(),
      outcome = "COMP",
      offenderCrn = "X320741",
      intendedProvider = "C21",
      eventId = 2500295343,
      requirementId = 2500083652,
      referralDate = LocalDate.of(2017, 6, 1),
      startDate = LocalDate.of(2017, 6, 1),
      endDate = LocalDate.of(2017, 12, 1),
      expectedStartDate = LocalDate.of(2017, 6, 1),
      expectedEndDate = LocalDate.of(2017, 12, 1),
      length = 1,
      notes = "bacon and eggs",
      manager = NewNsiManager(
        staff = "C00P017",
        team = "C00T02",
        provider = "C00",
      )
    )

    @JvmStatic
    fun validTestCases() = ValidationTestCaseBuilder.fromFactory(::validNsiFactory)
      .setValid()
      .kitchenSink()
      .allNull(NewNsi::expectedStartDate, NewNsi::expectedEndDate)
      .allNull(NewNsi::expectedEndDate)
      .allNull(NewNsi::notes)
      .allNull(NewNsi::eventId, NewNsi::requirementId)
      .allNull(NewNsi::requirementId, NewNsi::outcome, NewNsi::endDate)
      .allNull(NewNsi::eventId, NewNsi::requirementId, NewNsi::outcome, NewNsi::endDate)
      .add("Manager team & staff not provided") { it.copy(manager = it.manager.copy(team = null, staff = null)) }
      .add("Manager staff not provided") { it.copy(manager = it.manager.copy(staff = null)) }
      .cases

    @JvmStatic
    fun invalidTestCases() = ValidationTestCaseBuilder.fromFactory(::validNsiFactory)
      .string(NewNsi::type) { it.empty() }
      .string(NewNsi::subType) { it.empty() }
      .string(NewNsi::offenderCrn) { it.empty() }
      .number(NewNsi::eventId) { it.zero() }
      .number(NewNsi::requirementId) { it.zero().dependent(NewNsi::eventId) }
      .date(NewNsi::referralDate) { it.tomorrow() }
      .date(NewNsi::expectedStartDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::expectedEndDate) { it.before(NewNsi::expectedStartDate).dependent(NewNsi::expectedStartDate) }
      .date(NewNsi::startDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::startDate) { it.tomorrow() }
      .date(NewNsi::endDate) { it.before(NewNsi::startDate).dependent(NewNsi::startDate) }
      .number(NewNsi::length) { it.zero() }
      .string(NewNsi::status) { it.empty() }
      .dateTime(NewNsi::statusDate) { it.beforeDate(NewNsi::referralDate).tomorrow() }
      .string(NewNsi::outcome) { it.empty().dependent(NewNsi::endDate) }
      .string(NewNsi::intendedProvider) { it.empty() }
      .cases
  }

  @ParameterizedTest(name = "[{index}] Valid nsi {arguments}")
  @MethodSource("validTestCases")
  fun `Creating valid nsi`(case: ValidationTestCase<NewNsi>) {
    webTestClient.whenCreatingNsi(case.subject)
      .expectStatus().isCreated
      .expectBody()
      .shouldReturnCreatedNsi(case.subject)
      .shouldCreateEntityById(nsiRepository) { entity ->
        val observed = Fake.nsiMapper.toNew(Fake.nsiMapper.toDto(entity))

        assertThat(observed)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .comparingDateTimesToNearestSecond()
          .isEqualTo(case.subject)

        shouldAudit(
          AuditableInteraction.ADMINISTER_NSI,
          mapOf("offenderId" to entity.offender?.id, "nsiId" to entity.id)
        )

        shouldCreateSystemGeneratedContact(entity.id, entity.status?.contactTypeId!!)
      }
  }

  fun shouldCreateSystemGeneratedContact(nsiId: Long, typeId: Long) {
    val existing = contactRepository.findAllByNsiId(nsiId)
    assertThat(existing).anyMatch { it.type?.id == typeId }
  }

  @ParameterizedTest(name = "[{index}] Invalid nsi ({0})")
  @MethodSource("invalidTestCases")
  fun `Attempting to create invalid nsi`(case: ValidationTestCase<NewNsi>) {
    webTestClient.whenCreatingNsi(case.subject)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError(*case.invalidPaths.toTypedArray())

    shouldNotAudit(AuditableInteraction.ADMINISTER_NSI)
  }

  @Test
  fun `Attempting to create nsi without authentication`() {
    webTestClient.post().uri("/v1/nsi")
      .whenSendingUnauthenticatedRequest()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Attempting to create nsi with malformed json`() {
    webTestClient.post().uri("/v1/nsi")
      .whenSendingMalformedJson()
      .expectStatus().isBadRequest
      .expectBody().shouldReturnJsonParseError()
  }

  private fun WebTestClient.whenCreatingNsi(request: NewNsi) = this
    .post().uri("/v1/nsi")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()

  private fun WebTestClient.BodyContentSpec.shouldReturnCreatedNsi(request: NewNsi) = this
    .jsonPath("$.eventId").value(Matchers.equalTo(request.eventId))
    .jsonPath("$.requirementId").value(Matchers.equalTo(request.requirementId))
    .jsonPath("$.id").value(Matchers.greaterThan(0))
}
