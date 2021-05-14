package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ReplaceContact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ActiveProfiles("test-h2")
class ReplaceContactTest : IntegrationTestBase() {

  val request = Fake.replaceContact().copy(outcome = Fake.validNewContact().outcome!!)

  @Transactional
  @Test
  @Disabled
  fun `should successfully create a new contact`() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "C031"))
    webTestClient
      .whenReplacingContact(existingContact.id, request.copy(offenderId = 2500343964, nsiId = existingContact.nsiId,
        requirementId = existingContact.requirementId, eventId = existingContact.eventId))
      .expectStatus().isCreated
  }

  @Transactional
  @Test
  fun `should fail if existing contact is not an attendance contact`() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "TST01"))
    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Contact is not an Attendance Contact and can not be replaced")
  }

  @Transactional
  @Test
  fun `should fail if eventId does not match the contact eventId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "TST02"))
    webTestClient
      .whenReplacingContact(existingContact.id, request.copy(offenderId = 2500343964, eventId = 999))
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Event ID does not match the Event ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if requirement ID does not match the contact requirementId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "TST02"))
    webTestClient
      .whenReplacingContact(existingContact.id, request.copy(offenderId = 2500343964, requirementId = 999,
        nsiId = existingContact.nsiId, eventId = existingContact.eventId))
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Requirement ID does not match the Requirement ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if nsi ID does not match the contact NsiId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "TST02"))
    webTestClient
      .whenReplacingContact(existingContact.id, request.copy(offenderId = 2500343964, nsiId = 999,
        requirementId = existingContact.requirementId, eventId = existingContact.eventId))
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("NSI ID does not match the NSI ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if offender ID does not match the contact offender ID `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(type = "TST02"))
    webTestClient
      .whenReplacingContact(existingContact.id, request.copy(offenderId = 999, nsiId = existingContact.nsiId,
        requirementId = existingContact.requirementId, eventId = existingContact.eventId))
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Offender ID does not match the offender ID on the contact")
  }

  private fun WebTestClient.whenReplacingContact(id: Long, request: ReplaceContact) = this
    .post().uri("/v1/contact/$id/replace")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()
}
