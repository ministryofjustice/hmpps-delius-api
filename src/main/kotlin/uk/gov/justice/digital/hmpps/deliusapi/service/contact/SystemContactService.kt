package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.flattenLinkedContacts
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.updateNotes
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional(propagation = Propagation.MANDATORY)
class SystemContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
  private val nsiRepository: NsiRepository,
) {
  /**
   * Adds a system generated contact record.
   * System generated contacts are non-audited & non-validated (other than referential integrity of course).
   */
  fun createSystemContact(request: NewSystemContact) {
    val offender = offenderRepository.findByIdOrNull(request.offenderId)
      ?: throw IllegalArgumentException("Offender with id '${request.offenderId}' does not exist")
    val provider = providerRepository.findByIdOrNull(request.providerId)
      ?: throw IllegalArgumentException("Provider with id '${request.providerId}' does not exist")
    val team = provider.teams.find { it.id == request.teamId }
      ?: throw IllegalArgumentException("Team with id '${request.teamId}' does not exist on provider '${provider.code}'")
    val staff = team.staff.find { it.staff.id == request.staffId }?.staff
      ?: throw IllegalArgumentException("Staff with id '${request.staffId}' does not exist on team '${team.code}'")

    val event = if (request.eventId == null) null
    else offender.events?.find { it.id == request.eventId }
      ?: throw IllegalArgumentException("Event with id '${request.eventId}' does not exist on offender '${offender.id}'")

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw IllegalArgumentException("NSI with id '${request.nsiId}' does not exist")

    val type = when {
      request.typeId != null ->
        contactTypeRepository.findByIdOrNull(request.typeId)
          ?: throw IllegalArgumentException("Contact type with id '${request.typeId}' does not exist")
      request.type != null ->
        contactTypeRepository.findByCode(request.type.code)
          ?: throw IllegalArgumentException("Contact type with code '${request.type.code}' does not exist")
      else -> throw IllegalArgumentException("Must provide type id or code")
    }

    val entity = Contact(
      type = type,
      offender = offender,
      nsi = nsi,
      provider = provider,
      team = team,
      staff = staff,
      event = event,
      date = request.date,
      startTime = request.startTime,
    )

    entity.updateNotes(type.defaultHeadings, request.notes)

    contactRepository.saveAndFlush(entity)
  }

  fun createSystemEnforcementActionContact(contact: Contact): Contact? {
    val action = contact.enforcements.getOrNull(0)?.action ?: return null

    val entity = Contact(
      type = action.contactType,
      offender = contact.offender,
      nsi = contact.nsi,
      requirement = contact.requirement,
      event = contact.event,
      provider = contact.provider,
      team = contact.team,
      staff = contact.staff,
      officeLocation = contact.officeLocation,
      date = LocalDate.now(),
      startTime = LocalTime.now(),
      sensitive = contact.sensitive,
      alert = contact.alert,
      linkedContact = contact,
    )

    entity.updateNotes("${contact.notes}\n${LocalDateTime.now()}\nEnforcement Action: ${action.description}")

    return contactRepository.saveAndFlush(entity)
  }

  fun createLinkedSystemContact(contact: Contact, wellKnownType: WellKnownContactType): Contact {
    val type = contactTypeRepository.findByCode(wellKnownType.code)
      ?: throw RuntimeException("Cannot find the ${wellKnownType.name} well known contact type")

    val entity = Contact(
      type = type,
      offender = contact.offender,
      nsi = contact.nsi,
      requirement = contact.requirement,
      event = contact.event,
      provider = contact.provider,
      team = contact.team,
      staff = contact.staff,
      officeLocation = contact.officeLocation,
      date = contact.date,
      startTime = contact.startTime,
      sensitive = contact.sensitive,
      alert = contact.alert,
      linkedContact = contact,
    )

    return contactRepository.saveAndFlush(entity)
  }

  fun safeDeleteSystemContact(contact: Contact) {
    for (toDelete in listOf(contact, *contact.flattenLinkedContacts().toTypedArray())) {
      if (toDelete.maintainsFailureToComply() != MaintainFailureToComplyType.NONE) {
        throw RuntimeException("Cannot delete contact with id '${toDelete.id}' as it affects FTC")
      }
      contactRepository.delete(toDelete)
    }
  }
}
