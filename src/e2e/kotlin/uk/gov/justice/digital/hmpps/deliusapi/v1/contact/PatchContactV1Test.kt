
package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.ApiException
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Operation
import uk.gov.justice.digital.hmpps.deliusapi.util.assertThatException
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalTime
import kotlin.reflect.KProperty1

class PatchContactV1Test @Autowired constructor(
  private val repository: ContactRepository
) : EndToEndTest() {
  private lateinit var contact: ContactDto
  private lateinit var response: ContactDto

  @Test
  fun `Attempting to patch non-editable contact`() {
    contact = havingExistingContact(ContactTestsConfiguration::notUpdatable)
    val exception = assertThrows<ApiException> {
      whenPatchingContact(Operation("replace", "/notes", "Updated note"))
    }
    assertThatException(exception)
      .hasProperty(ApiException::statusCode, 400)
      .extractingObject { it.error!! }
      .hasProperty(ErrorResponse::userMessage, "Contact type '${contact.type}' is not editable")
  }

  @Test
  fun `Patching contact notes`() {
    ensureNoConflicts(ContactTestsConfiguration::updatable)

    contact = havingExistingContact(ContactTestsConfiguration::updatable)
    whenPatchingContact(Operation("replace", "/notes", "Updated note"))
    assertThat(response)
      .hasProperty(ContactDto::notes, "${contact.notes}\n\n---------\n\nUpdated note")
  }

  @Test
  fun `Patching contact outcome`() {
    ensureNoConflicts(ContactTestsConfiguration::updatable)

    val newOutcome = configuration.newContact(ContactTestsConfiguration::updatable).outcome
      ?: throw RuntimeException("Bad test data, the updatable contact should have an outcome")

    contact = havingExistingContact(ContactTestsConfiguration::updatable) {
      it.copy(outcome = null) // create a contact without the outcome
    }
    // patch in the outcome
    whenPatchingContact(Operation("replace", "/outcome", newOutcome))

    assertThat(contact)
      .hasProperty(ContactDto::outcome, null)
    assertThat(response)
      .hasProperty(ContactDto::outcome, newOutcome)
  }

  @Test
  fun `Patching contact enforcement`() {
    ensureNoConflicts(ContactTestsConfiguration::updatable)

    val configuredContact = configuration.newContact(ContactTestsConfiguration::enforcement)

    val startOutcome = configuration.newContact(ContactTestsConfiguration::updatable).outcome

    if (configuredContact.enforcement == null || configuredContact.outcome == null) {
      throw RuntimeException("Bad test data, the enforcement contact should have an outcome and enforcement")
    }

    contact = havingExistingContact(ContactTestsConfiguration::enforcement) {
      it.copy(outcome = startOutcome, enforcement = null) // create a contact with updatable outcome, that doesn't need an enforcement
    }
    // patch in the enforcement
    whenPatchingContact(Operation("replace", "/outcome", configuredContact.outcome), Operation("replace", "/enforcement", configuredContact.enforcement))

    assertThat(contact)
      .hasProperty(ContactDto::enforcement, null)
    shouldRecordEnforcementFlags(enforcementExpected = true)
  }

  private fun whenPatchingContact(vararg operations: Operation) {
    response = contactV1.safely { it.patchContact(contact.id, operations) }
  }

  private fun ensureNoConflicts(config: KProperty1<ContactTestsConfiguration, ContactTestConfiguration>) {
    val newContact = configuration.newContact(config)
    withDatabase {
      repository.deleteAllByOffenderCrnAndDateAndStartTimeAndEndTime(newContact.offenderCrn, newContact.date, LocalTime.parse(newContact.startTime), LocalTime.parse(newContact.endTime))
    }
  }

  private fun shouldRecordEnforcementFlags(enforcementExpected: Boolean = true) = withDatabase {
    withDatabase {
      val updated = repository.findByIdOrNull(response.id)
        ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

      val expectedEnforcementActionId = when (enforcementExpected) {
        true -> updated.enforcement?.action?.id
        else -> null
      }

      val expectedEnforcementDiary = when (enforcementExpected) {
        true -> true
        else -> null
      }

      assertThat(updated.enforcementDiary).isEqualTo(expectedEnforcementDiary)
      assertThat(updated.enforcementActionID).isEqualTo(expectedEnforcementActionId)

      if (enforcementExpected) {
        assertThat(updated.enforcement).isNotNull
      } else {
        assertThat(updated.enforcement).isNull()
      }
    }
  }
}
