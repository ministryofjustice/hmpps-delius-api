package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.ReplaceOperation
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.ContactService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import javax.validation.Validator

@ExtendWith(MockitoExtension::class)
class ContactControllerTest {
  @Mock private lateinit var service: ContactService
  @Spy private var mapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
  @Mock private lateinit var validator: Validator
  @Captor private lateinit var updateCaptor: ArgumentCaptor<UpdateContact>
  @InjectMocks private lateinit var subject: ContactController

  @Test
  fun `Creating contact`() {
    val request = Fake.newContact()
    val contact = Fake.contactDto()

    whenever(service.createContact(request)).thenReturn(contact)

    val observed = subject.createContact(request).body
    assertThat(observed).isSameAs(contact)
  }

  @Test
  fun `Patching contact`() {
    val id = Fake.faker.number().randomNumber()
    val existing = Fake.updateContact()
    val dto = Fake.contactDto()

    val op = ReplaceOperation(JsonPointer.of("outcome"), TextNode("next-outcome"))
    val patch = JsonPatch(listOf(op))
    whenever(service.getUpdateContact(id)).thenReturn(existing)
    whenever(validator.validate<UpdateContact>(any())).thenReturn(emptySet())
    whenever(service.updateContact(eq(id), capture(updateCaptor))).thenReturn(dto)

    val observed = subject.patchContact(id, patch)
    assertThat(observed).isSameAs(dto)
    assertThat(updateCaptor.value)
      .usingRecursiveComparison()
      .isEqualTo(existing.copy(outcome = "next-outcome"))
  }

  @Test
  fun `Deleting contact`() {
    val id = Fake.faker.number().randomNumber()

    subject.deleteContact(id)

    verify(service, times(1)).deleteContact(id)
  }

  @Test
  fun `Replace Contact`() {
    val id = Fake.faker.number().randomNumber()
    val replaceContact = Fake.replaceContact()
    subject.replaceContact(id, replaceContact)
    verify(service, times(1)).replaceContact(id, replaceContact)
  }
}
