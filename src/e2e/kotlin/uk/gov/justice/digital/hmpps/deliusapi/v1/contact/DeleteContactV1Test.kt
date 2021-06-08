package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.ApiException
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.assertThatException
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalTime

class DeleteContactV1Test@Autowired constructor(
  private val repository: ContactRepository
) : EndToEndTest() {
  private lateinit var contact: ContactDto

  @Test
  fun `Attempting to delete non-editable contact`() {
    contact = havingExistingContact(ContactTestsConfiguration::notUpdatable)
    val exception = assertThrows<ApiException> { whenDeletingContact() }
    assertThatException(exception)
      .hasProperty(ApiException::statusCode, 400)
      .extractingObject { it.error!! }
      .hasProperty(
        ErrorResponse::userMessage,
        "Contact with id '${contact.id}' cannot be deleted, contact type '${contact.type}' is not editable"
      )
  }

  @Test
  fun `Successfully deleting contact`() {
    withDatabase {
      val contactDetails = configuration.newContact(ContactTestsConfiguration::updatable)
      repository.deleteAllByOffenderCrnAndDateAndStartTimeAndEndTime(contactDetails.offenderCrn, contactDetails.date, LocalTime.parse(contactDetails.startTime), LocalTime.parse(contactDetails.endTime))
    }

    contact = havingExistingContact(ContactTestsConfiguration::updatable)
    assertDoesNotThrow { whenDeletingContact() }
  }

  private fun whenDeletingContact() {
    contactV1.safely { it.deleteContact(contact.id) }
  }
}
