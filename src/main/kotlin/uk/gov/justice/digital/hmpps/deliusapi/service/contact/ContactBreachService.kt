package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EventRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.models.LocalDateTimeWrapper
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType.Companion.BREACH_END_CODES
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType.Companion.BREACH_START_CODES
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStartDateTime
import java.time.LocalDate

@Service
class ContactBreachService(
  private val contactRepository: ContactRepository,
  private val eventRepository: EventRepository
) {
  /**
   * Determine whether inserting the specified contact will trigger or end a breach, update the linked event accordingly.
   * @param contact the contact that potentially triggered a breach.
   */
  fun updateBreachOnInsertContact(contact: Contact) {
    val event = contact.event ?: return // no event -> nothing to update

    // contact type must be a well known breach start or end type
    val contactType = WellKnownContactType.getBreachOrNull(contact.type.code) ?: return

    val contactStartDateTime = contact.getStartDateTime()
    var updated = false
    when (contactType.breachType) {
      BreachType.START -> {
        // Contact initiates breach

        // Prefer to go to the breach end contacts as a source of truth for the last breach date.
        // otherwise we attempt to construct from the event (which only has date no time)
        // otherwise there has been no previous breach
        val latestBreachEnd = contactRepository.findAllBreachDates(event.id, BREACH_END_CODES)
          .getOrNull(0)
          ?: if (event.breachEnd == null) null else LocalDateTimeWrapper(event.breachEnd!!, null)
        if (latestBreachEnd == null || contactStartDateTime >= latestBreachEnd.dateTime) {
          event.inBreach = true
          updated = true
        }
      }
      BreachType.END -> {
        // Contact ends breach

        val latestBreachStart = contactRepository.findAllBreachDates(event.id, BREACH_START_CODES)
          .getOrNull(0)
          ?.dateTime

        val contactFormsEndsBreach = latestBreachStart != null && latestBreachStart < contactStartDateTime
        val contactIsPostSentenceEvent = latestBreachStart?.toLocalDate() == contact.date &&
          listOf(WellKnownContactType.BREACH_PRISON_RECALL, WellKnownContactType.START_OF_POST_SENTENCE_SUPERVISION)
            .contains(contactType)
        event.inBreach = contactFormsEndsBreach && !contactIsPostSentenceEvent
        event.ftcCount = contactRepository.getCurrentFailureToComply(event)
        event.breachEnd = contact.date
        updated = true
      }
    }

    if (updated) {
      eventRepository.saveAndFlush(event)
    }
  }

  /**
   * Updates the breach meta on the event linked to the specified contact.
   * @param contact the updated contact that potentially triggered a breach.
   */
  fun updateBreachOnUpdateContact(contact: Contact) {
    val event = contact.event ?: return // no event -> nothing to update

    // contact type must be a well known breach start or end type
    val contactType = WellKnownContactType.getBreachOrNull(contact.type.code)
    if (contactType?.breachType == null) {
      return
    }

    val (breachStart, breachEnd) = getCurrentBreachDates(event.id)

    val inBreach = breachStart != null
    if (inBreach != event.inBreach || breachEnd != event.breachEnd) {
      event.inBreach = inBreach
      event.breachEnd = breachEnd
      event.ftcCount = contactRepository.getCurrentFailureToComply(event)
      eventRepository.saveAndFlush(event)
    }
  }

  private fun getCurrentBreachDates(eventId: Long): Pair<LocalDate?, LocalDate?> {
    val latestBreachStart = contactRepository.findAllBreachDates(eventId, BREACH_START_CODES)
      .getOrNull(0)
      ?.dateTime
      ?: return null to null

    val latestBreachEnd = contactRepository.findAllBreachDates(eventId, BREACH_END_CODES)
      .map { it.dateTime }
      .filter { it > latestBreachStart }
      .maxOrNull()
      ?.toLocalDate()

    return latestBreachStart.toLocalDate() to latestBreachEnd
  }
}
