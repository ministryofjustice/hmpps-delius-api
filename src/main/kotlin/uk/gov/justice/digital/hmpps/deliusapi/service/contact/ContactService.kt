package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.CreateOrUpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.extensions.findByIdOrNotFound
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCrnOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderRequestAuthority
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderResponseAuthority
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getEventOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getRequirementOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStaffOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getTeamOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.updateNotes

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
  private val nsiRepository: NsiRepository,
  private val mapper: ContactMapper,
  private val validation: ContactValidationService,
  private val systemContactService: SystemContactService,
  private val contactBreachService: ContactBreachService,
  private val contactEnforcementService: ContactEnforcementService,
) {
  fun getUpdateContact(id: Long): UpdateContact = mapper.toUpdate(getContact(id))

  @Transactional
  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.UPDATE_CONTACT)
  fun updateContact(id: Long, request: UpdateContact): ContactDto {
    val entity = getContact(id)

    if (entity.type.editable != true) {
      throw BadRequestException("Contact type '${entity.type.code}' is not editable")
    }

    val audit = AuditContext.get(AuditableInteraction.UPDATE_CONTACT)
    audit.contactId = entity.id

    validation.validateContactType(request, entity.type)
    val (provider, team, staff) = getProviderTeamStaff(request)

    // If contact is an attendance contact & has a start & end time then check for appointment clashes
    validation.validateFutureAppointmentClashes(request, entity.type, entity.offender, entity.id)

    entity.outcome = validation.validateOutcomeType(request, entity.type)
    validation.setOutcomeMeta(entity)

    entity.officeLocation = validation.validateOfficeLocation(request, entity.type, team)

    entity.provider = provider
    entity.team = team
    entity.staff = staff
    entity.date = request.date
    entity.startTime = request.startTime
    entity.endTime = request.endTime
    entity.alert = request.alert
    entity.sensitive = request.sensitive
    entity.description = request.description
    entity.updateNotes(request.notes)

    val createSystemEnforcementAction = if (request.enforcement != entity.enforcement?.action?.code) {
      entity.enforcement = validation.validateEnforcement(request, entity.outcome)
      true
    } else false

    contactRepository.saveAndFlush(entity)

    if (createSystemEnforcementAction) {
      val actionContact = systemContactService.createSystemEnforcementActionContact(entity)
      if (actionContact != null) {
        contactBreachService.updateBreachOnInsertContact(actionContact)
      }
    }

    contactBreachService.updateBreachOnUpdateContact(entity)
    contactEnforcementService.updateFailureToComply(entity)

    return mapper.toDto(entity)
  }

  @Transactional
  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.ADD_CONTACT)
  fun createContact(request: NewContact): ContactDto {
    val offender = offenderRepository.findByCrnOrBadRequest(request.offenderCrn)

    val audit = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    audit.offenderId = offender.id

    // General validation
    val type = contactTypeRepository.findSelectableByCode(request.type)
      ?: throw BadRequestException("Contact type with code '${request.type}' does not exist")

    validation.validateContactType(request, type)
    val outcome = validation.validateOutcomeType(request, type)
    val enforcement = validation.validateEnforcement(request, outcome)

    val (provider, team, staff) = getProviderTeamStaff(request)
    val officeLocation = validation.validateOfficeLocation(request, type, team)

    // If contact is an attendance contact & has a start & end time then check for appointment clashes
    validation.validateFutureAppointmentClashes(request, type, offender)

    // Associated entity validation
    val event = if (request.eventId == null) null else offender.getEventOrBadRequest(request.eventId)
    val requirement = if (event == null || request.requirementId == null) null
    else offender.getRequirementOrBadRequest(event, request.requirementId)

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw BadRequestException("NSI with id '${request.nsiId}' does not exist")

    validation.validateAssociatedEntity(type, requirement, event, nsi)

    val contact = Contact(
      offender = offender,
      nsi = nsi,
      type = type,
      outcome = outcome,
      provider = provider,
      team = team,
      staff = staff,
      event = event,
      requirement = requirement,
      officeLocation = officeLocation,
      date = request.date,
      startTime = request.startTime,
      endTime = request.endTime,
      alert = request.alert,
      sensitive = request.sensitive,
      description = request.description,
    )

    contact.updateNotes(type.defaultHeadings, request.notes)

    validation.setOutcomeMeta(contact)

    if (enforcement != null) {
      enforcement.contact = contact
      contact.enforcement = enforcement
    }

    val entity = contactRepository.saveAndFlush(contact)

    if (enforcement != null) {
      val actionContact = systemContactService.createSystemEnforcementActionContact(entity)
      if (actionContact != null) {
        contactBreachService.updateBreachOnInsertContact(actionContact)
      }
    }

    contactBreachService.updateBreachOnInsertContact(entity)
    contactEnforcementService.updateFailureToComply(entity)

    return mapper.toDto(entity)
  }

  @ProviderResponseAuthority
  private fun getContact(id: Long) = contactRepository.findByIdOrNotFound(id)

  private fun getProviderTeamStaff(request: CreateOrUpdateContact): Triple<Provider, Team, Staff> {
    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val team = provider.getTeamOrBadRequest(request.team)
    val staff = team.getStaffOrBadRequest(request.staff)
    return Triple(provider, team, staff)
  }
}
