package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Enforcement
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.CONTACT_NOTES_SEPARATOR
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.util.Optional

class CreateContactTest : ContactServiceTestBase() {
  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var request: NewContact

  private val savedContact = Fake.contact()
  private val enforcementContact = Fake.contact()

  @Test
  fun `Successfully creating contact`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation()
    havingEnforcementContact()

    whenSuccessfullyCreatingContact()

    shouldSaveContact()
    shouldSaveEnforcement()
    shouldSetAuditContext()
    shouldSetOutcomeMeta()
    shouldUpdateFailureToComply()
    shouldUpdateRarCounts()
  }

  @Test
  fun `Successfully creating contact without enforcement`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidEnforcement = null)

    whenSuccessfullyCreatingContact()

    shouldSaveContact()
    shouldNotSaveEnforcement()
    shouldSetAuditContext()
    shouldSetOutcomeMeta()
    shouldUpdateFailureToComply()
    shouldUpdateRarCounts()
  }

  @Test
  fun `Creating nsi contact`() {
    havingDependentEntities()
    type = type.apply { nsiTypes = listOf(nsi.type) }
    havingRepositories(havingNsi = true)
    request = request.copy(requirementId = null, eventId = null)
    havingValidation()
    passesValidation()
    shouldUpdateFailureToComply()
    shouldUpdateRarCounts()
  }

  @Test
  fun `Attempting to create contact with missing nsi`() {
    havingDependentEntities()
    havingRepositories(havingNsi = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing offender`() {
    havingDependentEntities()
    havingRepositories(havingOffender = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing type`() {
    havingDependentEntities()
    havingRepositories(havingType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing provider`() {
    havingDependentEntities()
    havingRepositories(havingProvider = false)
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing office location`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOfficeLocation = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid type`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid outcome type`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOutcomeType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Successfully creating contact with no outcome type`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOutcomeType = null)
    passesValidation()
  }

  @Test
  fun `Attempting to create contact with invalid enforcement`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidEnforcement = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid office location`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOfficeLocation = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Successfully creating contact with no office location`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOfficeLocation = null)
    passesValidation()
  }

  @Test
  fun `Attempting to create contact with clashing appointment`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidFutureAppointment = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid associated entity`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidAssociatedEntity = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid rar requirement`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidRarRequirement = false)
    shouldThrowBadRequest()
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingType: Boolean = true,
    havingProvider: Boolean = true,
    havingNsi: Boolean? = null,
  ) {
    request = Fake.newContact().copy(
      offenderCrn = offender.crn,
      nsiId = if (havingNsi == null) null else nsi.id,
      type = type.code,
      eventId = event.id,
      requirementId = requirement.id,
      outcome = outcome.code,
      officeLocation = officeLocation.code,
      staff = this.staff.code,
      team = team.code,
      provider = provider.code,
    )

    whenever(offenderRepository.findByCrn(offender.crn))
      .thenReturn(if (havingOffender) offender else null)
    whenever(contactTypeRepository.findSelectableByCode(type.code))
      .thenReturn(if (havingType) type else null)
    whenever(providerRepository.findByCodeAndSelectableIsTrue(provider.code))
      .thenReturn(if (havingProvider) provider else null)
    whenever(nsiRepository.findById(nsi.id))
      .thenReturn(if (havingNsi == true) Optional.of(nsi) else Optional.empty())
  }

  private fun havingValidation(
    havingValidType: Boolean = true,
    havingValidOutcomeType: Boolean? = true,
    havingValidEnforcement: Boolean? = true,
    havingValidOfficeLocation: Boolean? = true,
    havingValidFutureAppointment: Boolean = true,
    havingValidAssociatedEntity: Boolean = true,
    havingValidRarRequirement: Boolean = true,
  ) {
    if (!havingValidType) {
      whenever(validationService.validateContactType(request, type))
        .thenThrow(BadRequestException("bad contact type"))
    }

    val outcomeMock = whenever(validationService.validateOutcomeType(request, type))

    when (havingValidOutcomeType) {
      null -> outcomeMock.thenReturn(null)
      true -> outcomeMock.thenReturn(outcome)
      false -> outcomeMock.thenThrow(BadRequestException("bad outcome"))
    }

    val enforcementMock = whenever(validationService.validateEnforcement(request, outcome))
    when (havingValidEnforcement) {
      null -> enforcementMock.thenReturn(null)
      true -> enforcementMock.thenReturn(enforcement)
      false -> enforcementMock.thenThrow(BadRequestException("bad enforcement"))
    }

    val officeLocationMock = whenever(validationService.validateOfficeLocation(request, type, team))
    when (havingValidOfficeLocation) {
      null -> officeLocationMock.thenReturn(null)
      true -> officeLocationMock.thenReturn(officeLocation)
      false -> officeLocationMock.thenThrow(BadRequestException("bad office location"))
    }

    if (!havingValidFutureAppointment) {
      whenever(validationService.validateFutureAppointmentClashes(request, type, offender, null))
        .thenThrow(BadRequestException("bad future appointment"))
    }

    if (!havingValidAssociatedEntity) {
      whenever(validationService.validateAssociatedEntity(type, requirement, event, null))
        .thenThrow(BadRequestException("bad associated entity"))
    }

    if (!havingValidRarRequirement) {
      whenever(validationService.validateRarRequirement(request, type, requirement, null))
        .thenThrow(BadRequestException("bad rar requirement"))
    }
  }

  private fun havingEnforcementContact() {
    whenever(systemContactService.createSystemEnforcementActionContact(savedContact))
      .thenReturn(enforcementContact)
  }

  private fun whenSuccessfullyCreatingContact() {
    val expectedResult = Fake.contactDto()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(savedContact)
    whenever(mapper.toDto(savedContact)).thenReturn(expectedResult)

    val observed = subject.createContact(request)
    assertThat(observed).isSameAs(expectedResult)
  }

  private fun shouldThrowBadRequest() {
    assertThrows<BadRequestException> {
      subject.createContact(request)
    }
  }

  private fun passesValidation() {
    whenever(contactRepository.saveAndFlush(entityCaptor.capture())).thenReturn(savedContact)

    subject.createContact(request)

    verify(contactRepository).saveAndFlush(any())
  }

  private fun shouldSetOutcomeMeta() {
    // should set outcome meta
    verify(validationService, times(1)).setOutcomeMeta(entityCaptor.value)
  }

  private fun shouldSaveContact() {
    assertThat(entityCaptor.value)
      .hasProperty(Contact::offender, offender)
      .hasProperty(Contact::type, type)
      .hasProperty(Contact::outcome, outcome)
      .hasProperty(Contact::provider, provider)
      .hasProperty(Contact::team, team)
      .hasProperty(Contact::staff, staff)
      .hasProperty(Contact::event, event)
      .hasProperty(Contact::requirement, requirement)
      .hasProperty(Contact::officeLocation, officeLocation)
      .hasProperty(Contact::date, request.date)
      .hasProperty(Contact::startTime, request.startTime)
      .hasProperty(Contact::endTime, request.endTime)
      .hasProperty(Contact::alert, request.alert)
      .hasProperty(Contact::sensitive, request.sensitive)
      .hasProperty(Contact::notes, type.defaultHeadings + CONTACT_NOTES_SEPARATOR + request.notes)
      .hasProperty(Contact::description, request.description)
      .hasProperty(Contact::partitionAreaId, 0)
      .hasProperty(Contact::staffEmployeeId, 1)
      .hasProperty(Contact::teamProviderId, 1)
      .hasProperty(Contact::hoursCredited, null)
      .hasProperty(Contact::attended, null)
      .hasProperty(Contact::complied, null)
  }

  private fun shouldSaveEnforcement() {
    assertThat(entityCaptor.value)
      .hasProperty(Contact::enforcements, listOf(enforcement))
    assertThat(enforcement)
      .hasProperty(Enforcement::contact, entityCaptor.value)
    verify(systemContactService, times(1))
      .createSystemEnforcementActionContact(savedContact)
    verify(contactBreachService, times(1)).updateBreachOnInsertContact(enforcementContact)
    verify(contactBreachService, times(1)).updateBreachOnInsertContact(savedContact)
  }

  private fun shouldNotSaveEnforcement() {
    assertThat(entityCaptor.value)
      .hasProperty(Contact::enforcements, emptyList())
    verify(systemContactService, never())
      .createSystemEnforcementActionContact(any())
  }

  private fun shouldSetAuditContext() {
    val context = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    assertThat(context.offenderId).isEqualTo(offender.id)
  }

  private fun shouldUpdateFailureToComply() =
    verify(contactEnforcementService, times(1)).updateFailureToComply(savedContact)

  private fun shouldUpdateRarCounts() =
    verify(contactRarService, times((1))).updateRarCounts(savedContact)
}
