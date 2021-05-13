package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

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

  @Transactional
  @Test
  fun `should successfully create a new contact`() {
    val existingContact = havingExistingContact()
    val request = Fake.replaceContact().copy(outcome = Fake.validNewContact().outcome!!)
    webTestClient
      .whenReplacingContact(existingContact.id, request)
      .expectStatus().isCreated
  }

  private fun WebTestClient.whenReplacingContact(id: Long, request: ReplaceContact) = this
    .post().uri("/v1/contact/$id/replace")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()
}
