package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.RequirementTypeCategory.Companion.RAR_REQUIREMENT_TYPE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class ContactRarServiceTest {
  @Mock
  private lateinit var contactRepository: ContactRepository

  @Mock
  private lateinit var requirementRepository: RequirementRepository

  @Mock
  private lateinit var nsiRepository: NsiRepository

  @InjectMocks
  private lateinit var contactRarService: ContactRarService

  @Captor
  private lateinit var requirementEntityCaptor: ArgumentCaptor<Requirement>

  @Captor
  private lateinit var nsiEntityCaptor: ArgumentCaptor<Nsi>

  @Test
  fun `updateRarCounts saves new requirement RAR count when contact has RAR requirement`() {
    val contact = Fake.contact().apply {
      requirement!!.typeCategory!!.code = RAR_REQUIREMENT_TYPE_CATEGORY_CODE
      requirement!!.rarCount = 0
    }

    whenever(contactRepository.countRequirementRar(contact.requirement!!.id)).thenReturn(5)
    whenever(requirementRepository.saveAndFlush(requirementEntityCaptor.capture())).thenReturn(contact.requirement)

    // when
    contactRarService.updateRarCounts(contact)

    // saves new count
    assertThat(requirementEntityCaptor.value.rarCount).isEqualTo(5)

    verify(requirementRepository, times(1)).saveAndFlush(requirementEntityCaptor.value)
    verifyZeroInteractions(nsiRepository)
  }

  @Test
  fun `updateRarCounts doesn't update RAR count when contact has non-RAR requirement`() {
    val contact = Fake.contact().apply {
      requirement!!.typeCategory!!.code = "X"
      requirement!!.rarCount = 0
    }

    // when
    contactRarService.updateRarCounts(contact)

    verifyZeroInteractions(contactRepository)
    verifyZeroInteractions(requirementRepository)
    verifyZeroInteractions(nsiRepository)
  }

  @Test
  fun `updateRarCounts saves new nsi RAR count when contact has NSI with RAR requirement`() {
    val contact = Fake.contact().apply {
      nsi = Fake.nsi().apply {
        rarCount = 3
        requirement!!.typeCategory!!.code = RAR_REQUIREMENT_TYPE_CATEGORY_CODE
      }
      requirement = null
    }

    whenever(contactRepository.countNsiRar(contact.nsi!!.id)).thenReturn(9)
    whenever(nsiRepository.saveAndFlush(nsiEntityCaptor.capture())).thenReturn(contact.nsi)

    // when
    contactRarService.updateRarCounts(contact)

    // saves new count
    assertThat(nsiEntityCaptor.value.rarCount).isEqualTo(9)

    verify(nsiRepository, times(1)).saveAndFlush(nsiEntityCaptor.value)
    verifyZeroInteractions(requirementRepository)
  }

  @Test
  fun `updateRarCounts doesn't update RAR count when contact has non-RAR NSI`() {
    val contact = Fake.contact().apply {
      nsi = Fake.nsi().apply {
        rarCount = 3
        requirement!!.typeCategory!!.code = "X"
      }
      requirement = null
    }

    // when
    contactRarService.updateRarCounts(contact)

    verifyZeroInteractions(contactRepository)
    verifyZeroInteractions(requirementRepository)
    verifyZeroInteractions(nsiRepository)
  }
}
