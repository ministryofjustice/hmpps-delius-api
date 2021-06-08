package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.util.Optional

class DeleteContactTest : ContactServiceTestBase() {
  private lateinit var contact: Contact
  private lateinit var linked: Contact

  @Test
  fun `Successfully deleting past contact`() {
    havingContact()
    whenDeletingContact()
    shouldDeleteContacts()
    shouldUpdateBreach()
    shouldUpdateFtc()
    shouldUpdateRarCounts()
    shouldSetAuditContext(AuditableInteraction.DELETE_PREVIOUS_CONTACT)
    shouldAssertProviderAuthority(contact.provider!!.code)
  }

  @Test
  fun `Successfully deleting future contact`() {
    havingContact(date = LocalDate.now())
    whenDeletingContact()
    shouldDeleteContacts()
    shouldUpdateBreach()
    shouldUpdateFtc()
    shouldUpdateRarCounts()
    shouldSetAuditContext(AuditableInteraction.DELETE_CONTACT)
    shouldAssertProviderAuthority(contact.provider!!.code)
  }

  @Test
  fun `Attempting to delete missing contact`() {
    val id = Fake.faker.number().randomNumber()
    assertThrows<NotFoundException> { subject.deleteContact(id) }
  }

  @Test
  fun `Attempting to delete non-editable contact`() {
    havingContact(editable = false)
    assertThrows<BadRequestException> { whenDeletingContact() }
  }

  private fun havingContact(editable: Boolean = true, date: LocalDate = LocalDate.now().minusDays(1)) {
    linked = Fake.contact()
    contact = Fake.contact().apply {
      this.date = date
      type.editable = editable
      linkedContacts.add(linked)
    }
    whenever(contactRepository.findById(contact.id)).thenReturn(Optional.of(contact))
  }

  private fun whenDeletingContact() = subject.deleteContact(contact.id)

  private fun shouldDeleteContacts() =
    verify(contactRepository, times(1)).deleteAll(eq(listOf(contact, linked)))

  private fun shouldUpdateBreach() {
    verify(contactBreachService, times(1)).updateBreachOnUpdateContact(contact)
    verify(contactBreachService, times(1)).updateBreachOnUpdateContact(linked)
  }

  private fun shouldUpdateFtc() =
    verify(contactEnforcementService, times(1)).updateFailureToComply(contact)

  private fun shouldUpdateRarCounts() =
    verify(contactRarService, times((1))).updateRarCounts(contact)

  private fun shouldSetAuditContext(interaction: AuditableInteraction) {
    val context = AuditContext.get(interaction)
    assertThat(context)
      .hasProperty(AuditContext::contactId, contact.id)
  }
}
