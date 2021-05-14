package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ReplaceContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.util.Optional

class ReplaceContactServiceTest : ContactServiceTestBase() {
  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var existingContact: Contact
  private lateinit var request: ReplaceContact

  @Test
  fun `Successfully replaces contact`() {
    // Given valid data
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation()

    // When I attempt to replace a contact
    whenReplacingContact()

    // Then save is called twice (once for create, once for update)
    verify(contactRepository, times(2)).saveAndFlush(any())
    val capturedUpdateContact = entityCaptor.allValues[0]
    val capturedCreateContact = entityCaptor.allValues[1]

    // And the values have been set correctly
    assertThat(capturedUpdateContact.outcome?.code)
      .describedAs("Outcome is updated on the existing contact")
      .isEqualTo(request.outcome)
    assertThat(capturedCreateContact)
      .describedAs("Dates/times are set correctly on the new contact")
      .hasFieldOrPropertyWithValue("date", request.date)
      .hasFieldOrPropertyWithValue("startTime", request.startTime)
      .hasFieldOrPropertyWithValue("endTime", request.endTime)
  }

  @Test
  fun `Returns not found when contact doesn't exist`() {
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories(havingContact = false)
    havingValidation()

    assertThrows<NotFoundException> {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when outcome is invalid`() {
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidOutcomeType = false)

    assertThrows<BadRequestException>("bad outcome") {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when contact type is not an attendance contact`(){
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidContactAttendanceType = false)

    assertThrows<BadRequestException>("not an attendance contact") {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when contact id does not match the contact contactId`(){
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidOffenderId = false)

    assertThrows<BadRequestException>("offender ID does not match") {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when event id does not match contact eventId`(){
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidEventId = false)

    assertThrows<BadRequestException>("event ID does not match") {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when requirement id does not match contact requirementId`(){
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidRequirementId = false)

    assertThrows<BadRequestException>("requirement ID does not match") {
      whenReplacingContact()
    }
  }

  @Test
  fun `Fails when NSI id does not match contact NSI Id`(){
    havingDependentEntities()
    havingMappedContacts()
    havingRepositories()
    havingValidation(havingValidNsiId = false)

    assertThrows<BadRequestException>("NSI ID does not match") {
      whenReplacingContact()
    }
  }

  private fun havingMappedContacts(
    existingContact: Contact = Fake.contact(),
    updateContact: UpdateContact = Fake.updateContact().copy(
      team = team.code,
      staff = staff.code,
    ),
    newContact: NewContact = Fake.newContact().copy(
      offenderCrn = offender.crn,
      eventId = event.id,
      requirementId = null,
      nsiId = null,
      type = type.code,
      team = team.code,
      staff = staff.code,
    )
  ) {
    this.existingContact = existingContact
    whenever(mapper.toUpdate(existingContact)).thenReturn(updateContact)
    val dto = Fake.contactDto()
    whenever(mapper.toDto(existingContact)).thenReturn(dto)
    whenever(mapper.toNew(dto)).thenReturn(newContact)
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingContact: Boolean = true,
    havingType: Boolean = true,
    havingProvider: Boolean = true,
    havingNsi: Boolean? = null,
  ) {
    whenever(offenderRepository.findByCrn(offender.crn))
      .thenReturn(if (havingOffender) offender else null)
    whenever(contactRepository.findById(existingContact.id))
      .thenReturn(if (havingContact) Optional.of(existingContact) else Optional.empty())
    whenever(contactTypeRepository.findSelectableByCode(type.code))
      .thenReturn(if (havingType) type else null)
    whenever(providerRepository.findByCodeAndSelectableIsTrue(any()))
      .thenReturn(if (havingProvider) provider else null)
    whenever(nsiRepository.findById(nsi.id))
      .thenReturn(if (havingNsi == true) Optional.of(nsi) else Optional.empty())

    whenever(contactRepository.saveAndFlush(entityCaptor.capture())).thenReturn(Fake.contact())
  }

  private fun havingValidation(
    havingValidOutcomeType: Boolean? = true,
    havingValidContactAttendanceType: Boolean? = true,
    havingValidOffenderId: Boolean? = true,
    havingValidEventId: Boolean? = true,
    havingValidRequirementId: Boolean? = true,
    havingValidNsiId: Boolean? =true,
  ) {
    val outcomeMock = whenever(validationService.validateOutcomeType(any(), any()))

    when (havingValidOutcomeType) {
      null -> outcomeMock.thenReturn(null)
      true -> outcomeMock.thenReturn(outcome)
      false -> outcomeMock.thenThrow(BadRequestException("bad outcome"))
    }

    if (havingValidContactAttendanceType == false) {
      whenever(validationService.validateReplaceContactType(any())).thenThrow(BadRequestException("not an attendance contact"))
    }

    if (havingValidOffenderId == false) {
      whenever(validationService.validateReplaceContactOffenderId(any(),any())).thenThrow(BadRequestException("offender ID does not match"))
    }

    if (havingValidEventId == false) {
      whenever(validationService.validateReplaceContactEventId(any(),any())).thenThrow(BadRequestException("event ID does not match"))
    }

    if (havingValidRequirementId == false) {
      whenever(validationService.validateReplaceContactRequirementId(any(),any())).thenThrow(BadRequestException("event ID does not match"))
    }

    if (havingValidNsiId == false) {
      whenever(validationService.validateReplaceContactNsiId(any(),any())).thenThrow(BadRequestException("NSI ID does not match"))
    }
  }

//  private fun havingAttendanceTypeValidation(
//    havingValidContactAttendanceType: Boolean? = true,
//  ){
//    if (havingValidContactAttendanceType == false) {
//      whenever(validationService.validateReplaceContactType(any())).thenThrow(BadRequestException("not an attendance contact"))
//    }
//  }

//  private fun havingOffenderIdValidation(
//    havingValidOffenderId: Boolean? =true,
//  ){
//    if (havingValidOffenderId == false) {
//      whenever(validationService.validateReplaceContactOffenderId(any(),any())).thenThrow(BadRequestException("offender ID does not match"))
//    }
//  }

  private fun whenReplacingContact() {
    request = Fake.replaceContact().copy(outcome = outcome.code)
    subject.replaceContact(existingContact.id, request)
  }
}
