package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ReplaceContact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ActiveProfiles("test-h2")
class ReplaceContactTest : IntegrationTestBase() {

  @Autowired
  lateinit var contactRepository: ContactRepository

  @Transactional
  @Test
  fun `should successfully create a new contact`() {
    val existingContact = havingExistingContact(
      Fake.validNewContact().copy(
        outcome = null,
        type = "TST06",
        officeLocation = "C00OFFB"
      )
    )
    val request = havingValidRequest(existingContact)
    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isCreated
      .expectBody()
      .jsonPath("$.outcome").doesNotExist()
      .jsonPath("$.officeLocation").value(Matchers.equalTo("C00OFFB"))
      .shouldCreateEntityById(contactRepository) {
        assertThat(it.officeLocation?.code).isEqualTo("C00OFFB")
      }
  }

  private fun havingValidRequest(existingContact: ContactDto): ReplaceContact {
    return Fake.replaceContact().copy(
      outcome = Fake.validNewContact().outcome!!,
      offenderCrn = existingContact.offenderCrn,
      nsiId = existingContact.nsiId,
      requirementId = existingContact.requirementId,
      eventId = existingContact.eventId,
      officeLocation = null,
    )
  }

  @Transactional
  @Test
  fun `should fail if existing contact is not an attendance contact`() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(outcome = null, type = "TST01"))
    val request = havingValidRequest(existingContact)

    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Contact is not an Attendance Contact and can not be replaced")
  }

  @Transactional
  @Test
  fun `should fail if eventId does not match the contact eventId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(outcome = null, type = "TST02"))
    val request = havingValidRequest(existingContact).copy(eventId = Fake.id())

    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Event ID does not match the Event ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if requirement ID does not match the contact requirementId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(outcome = null, type = "TST02"))
    val request = havingValidRequest(existingContact).copy(requirementId = Fake.id())

    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Requirement ID does not match the Requirement ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if nsi ID does not match the contact NsiId `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(outcome = null, type = "TST02"))
    val request = havingValidRequest(existingContact).copy(nsiId = Fake.id())

    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("NSI ID does not match the NSI ID on the contact")
  }

  @Transactional
  @Test
  fun `should fail if offender ID does not match the contact offender ID `() {
    val existingContact = havingExistingContact(Fake.validNewContact().copy(outcome = null, type = "TST02"))
    val request = havingValidRequest(existingContact).copy(offenderCrn = Fake.crn())

    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError("Offender CRN does not match the offender CRN on the contact")
  }

  private fun WebTestClient.whenReplacingContact(id: Long, request: ReplaceContact) = this
    .post().uri("/v1/contact/$id/replace")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()
}
