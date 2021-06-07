package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.CONTACT_NOTES_SEPARATOR
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.util.Optional

class UpdateContactTest : ContactServiceTestBase() {
  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var request: UpdateContact
  private lateinit var contact: Contact

  @Test
  fun `Attempting to update missing contact`() {
    havingDependentEntities()
    havingContact(having = false)
    assertThrows<NotFoundException> { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update non-editable contact`() {
    havingDependentEntities()
    havingContact(editable = false)
    assertThrows<BadRequestException> { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact and invalidating type`() {
    havingDependentEntities()
    havingContact()
    whenever(validationService.validateContactType(request, contact.type))
      .thenThrow(BadRequestException("bad contact type"))
    assertThrows<BadRequestException>("bad contact type") { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact and invalidating rar requirement`() {
    havingDependentEntities()
    havingContact()
    whenever(validationService.validateRarRequirement(request, contact.type, contact.requirement, contact.nsi))
      .thenThrow(BadRequestException("bad rar requirement"))
    assertThrows<BadRequestException>("bad rar requirement") { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with missing provider`() {
    havingDependentEntities()
    havingContact()
    havingProvider(having = false)
    assertThrows<BadRequestException> { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingContact()
    havingProvider()
    assertThrows<BadRequestException> { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingContact()
    havingProvider()
    assertThrows<BadRequestException> { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with clashing appointments`() {
    havingDependentEntities()
    havingContact()
    havingProvider()
    whenever(validationService.validateFutureAppointmentClashes(request, contact.type, contact.offender, contact.id))
      .thenThrow(BadRequestException("clashing appointments"))
    assertThrows<BadRequestException>("clashing appointments") { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with invalid outcome type`() {
    havingDependentEntities()
    havingContact()
    havingProvider()
    havingOutcomeType(having = false)
    assertThrows<BadRequestException>("bad outcome type") { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with invalid enforcement`() {
    havingDependentEntities()
    havingContact()
    havingProvider()
    havingOutcomeType()
    havingEnforcement(having = false)
    assertThrows<BadRequestException>("bad enforcement") { whenUpdatingContact() }
  }

  @Test
  fun `Attempting to update contact with invalid office location`() {
    havingDependentEntities()
    havingContact()
    havingProvider()
    havingOutcomeType()
    havingOfficeLocation(having = false)
    assertThrows<BadRequestException>("bad office location") { whenUpdatingContact() }
  }

  @Test
  fun `Updating contact`() {
    val dto = Fake.contactDto()

    havingDependentEntities()
    havingContact()
    val originalProviderCode = contact.provider!!.code
    havingProvider()
    havingOutcomeType()
    havingEnforcement()
    havingOfficeLocation()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture())).thenReturn(contact)
    whenever(mapper.toDto(contact)).thenReturn(dto)

    val actionContact = Fake.contact()
    whenever(systemContactService.createSystemEnforcementActionContact(contact)).thenReturn(actionContact)

    val originalNotes = contact.notes

    val observed = whenUpdatingContact()

    assertThat(observed).isSameAs(dto)
    assertThat(entityCaptor.value)
      .isSameAs(contact)
      .hasProperty(Contact::outcome, outcome)
      .hasProperty(Contact::enforcements, listOf(enforcement))
      .hasProperty(Contact::officeLocation, officeLocation)
      .hasProperty(Contact::provider, provider)
      .hasProperty(Contact::team, team)
      .hasProperty(Contact::staff, staff)
      .hasProperty(Contact::date, request.date)
      .hasProperty(Contact::startTime, request.startTime)
      .hasProperty(Contact::endTime, request.endTime)
      .hasProperty(Contact::alert, request.alert)
      .hasProperty(Contact::sensitive, request.sensitive)
      .hasProperty(Contact::description, request.description)
      .hasProperty(Contact::notes, originalNotes + CONTACT_NOTES_SEPARATOR + request.notes)

    shouldSetAuditContext()

    // should set outcome meta
    verify(validationService, times(1)).setOutcomeMeta(entityCaptor.value)

    // should create system enforcements
    verify(systemContactService, times(1)).createSystemEnforcementActionContact(entityCaptor.value)

    // should update breach
    verify(contactBreachService, times(1)).updateBreachOnInsertContact(actionContact)
    verify(contactBreachService, times(1)).updateBreachOnUpdateContact(entityCaptor.value)

    // should update ftc
    verify(contactEnforcementService, times(1)).updateFailureToComply(entityCaptor.value)

    verify(contactRarService, times(1)).updateRarCounts(entityCaptor.value)

    shouldAssertProviderAuthority(originalProviderCode)
  }

  private fun havingContact(having: Boolean = true, editable: Boolean = true) {
    contact = Fake.contact().apply {
      type = Fake.contactType().apply { this.editable = editable }
    }
    request = Fake.updateContact().copy(
      team = team.code,
      staff = staff.code,
    )
    whenever(contactRepository.findById(contact.id))
      .thenReturn(if (having) Optional.of(contact) else Optional.empty())
  }

  private fun havingProvider(having: Boolean = true) {
    whenever(providerRepository.findByCodeAndSelectableIsTrue(request.provider))
      .thenReturn(if (having) provider else null)
  }

  private fun havingOutcomeType(having: Boolean = true) {
    val mock = whenever(validationService.validateOutcomeType(request, contact.type))
    if (having) mock.thenReturn(outcome) else mock.thenThrow(BadRequestException("bad outcome"))
  }

  private fun havingEnforcement(having: Boolean = true) {
    val mock = whenever(validationService.validateEnforcement(request, outcome))
    if (having) {
      mock.thenReturn(enforcement)
    } else {
      mock.thenThrow(BadRequestException("bad enforcement"))
    }
  }

  private fun havingOfficeLocation(having: Boolean = true) {
    val mock = whenever(validationService.validateOfficeLocation(request, contact.type, team))
    if (having) {
      mock.thenReturn(officeLocation)
    } else {
      mock.thenThrow(BadRequestException("bad office location"))
    }
  }

  private fun whenUpdatingContact() = subject.updateContact(contact.id, request)

  private fun shouldSetAuditContext() {
    val context = AuditContext.get(AuditableInteraction.UPDATE_CONTACT)
    assertThat(context.contactId).isEqualTo(contact.id)
  }
}
