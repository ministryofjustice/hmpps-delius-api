package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.assertj.core.api.ObjectAssert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Enforcement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemContactServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @Mock private lateinit var offenderRepository: OffenderRepository
  @Mock private lateinit var contactTypeRepository: ContactTypeRepository
  @Mock private lateinit var providerRepository: ProviderRepository
  @Mock private lateinit var nsiRepository: NsiRepository
  @InjectMocks private lateinit var subject: SystemContactService

  private lateinit var type: ContactType
  private lateinit var outcome: ContactOutcomeType
  private lateinit var enforcement: Enforcement
  private lateinit var offender: Offender
  private lateinit var provider: Provider
  private lateinit var staff: Staff
  private lateinit var team: Team
  private lateinit var officeLocation: OfficeLocation
  private lateinit var event: Event
  private lateinit var requirement: Requirement
  private lateinit var nsi: Nsi

  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var request: NewSystemContact

  @Test
  fun `Successfully creating system contact with type by id`() {
    havingDependentEntities()
    havingRepositories()
    whenCreatingSystemContactShouldSuccessfullySaveContact()
  }

  @Test
  fun `Successfully creating system contact with type by code`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = null, havingTypeByCode = true)
    whenCreatingSystemContactShouldSuccessfullySaveContact()
  }

  @Test
  fun `Attempting to create system contact with missing type by id`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing type by code`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = null, havingTypeByCode = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing offender`() {
    havingDependentEntities()
    havingRepositories(havingOffender = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing provider`() {
    havingDependentEntities()
    havingRepositories(havingProvider = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system enforcement action contact without enforcement action`() {
    val enforcement = Fake.enforcement().apply { action = null }
    val contact = Fake.contact().apply { enforcements = mutableListOf(enforcement) }
    subject.createSystemEnforcementActionContact(contact)
    verify(contactRepository, never()).saveAndFlush(any())
  }

  @Test
  fun `Successfully creating system enforcement action contact`() {
    val contact = Fake.contact()
    val action = contact.enforcements.getOrNull(0)?.action!!
    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(contact)

    subject.createSystemEnforcementActionContact(contact)

    assertThat(entityCaptor.value)
      .hasProperty(Contact::type, action.contactType)
      .hasProperty(Contact::date, LocalDate.now())
      .hasCommonContactSourcedProperties(contact)

    assertThat(entityCaptor.value.notes)
      .startsWith(contact.notes)
      .endsWith("Enforcement Action: ${action.description}")

    assertThat(entityCaptor.value.startTime!!)
      .isCloseTo(LocalTime.now(), within(1, ChronoUnit.MINUTES))
  }

  @Test
  fun `Successfully creating system linked contact but missing contact type`() {
    val contact = Fake.contact()
    val type = Fake.faker.options().option(WellKnownContactType::class.java)
    whenever(contactTypeRepository.findByCode(type.code)).thenReturn(null)
    assertThrows<RuntimeException> { subject.createLinkedSystemContact(contact, type) }
  }

  @Test
  fun `Successfully creating system linked contact`() {
    val contact = Fake.contact()
    val savedContact = Fake.contact()
    val code = Fake.faker.options().option(WellKnownContactType::class.java)
    val contactType = Fake.contactType()
    whenever(contactTypeRepository.findByCode(code.code)).thenReturn(contactType)
    whenever(contactRepository.saveAndFlush(entityCaptor.capture())).thenReturn(savedContact)
    val observed = subject.createLinkedSystemContact(contact, code)
    assertThat(observed).isSameAs(savedContact)
    assertThat(entityCaptor.value)
      .hasProperty(Contact::type, contactType)
      .hasProperty(Contact::date, contact.date)
      .hasProperty(Contact::startTime, contact.startTime)
      .hasCommonContactSourcedProperties(contact)
  }

  @Test
  fun `Successfully safe deleting system contact`() {
    val linked2 = Fake.contact().apply { type.nationalStandardsContact = false }
    val linked1 = Fake.contact().apply {
      linkedContacts.add(linked2)
      type.nationalStandardsContact = false
    }
    linked2.linkedContacts.add(linked1) // function must deal with bad, recursive linked contacts
    val contact = Fake.contact().apply {
      linkedContacts.add(linked1)
      type.nationalStandardsContact = false
    }

    subject.safeDeleteSystemContact(contact)

    verify(contactRepository, times(1)).delete(contact)
    verify(contactRepository, times(1)).delete(linked1)
    verify(contactRepository, times(1)).delete(linked2)
  }

  @Test
  fun `Attempting to safe delete system contact but maintains FTC`() {
    val linked = Fake.contact().apply {
      type.nationalStandardsContact = true
    }
    val contact = Fake.contact().apply {
      linkedContacts.add(linked)
      type.nationalStandardsContact = false
    }

    assertThrows<RuntimeException> { subject.safeDeleteSystemContact(contact) }
  }

  private fun ObjectAssert<Contact>.hasCommonContactSourcedProperties(contact: Contact) = this
    .hasProperty(Contact::offender, contact.offender)
    .hasProperty(Contact::outcome, null)
    .hasProperty(Contact::provider, contact.provider)
    .hasProperty(Contact::team, contact.team)
    .hasProperty(Contact::staff, contact.staff)
    .hasProperty(Contact::nsi, contact.nsi)
    .hasProperty(Contact::requirement, contact.requirement)
    .hasProperty(Contact::event, contact.event)
    .hasProperty(Contact::officeLocation, contact.officeLocation)
    .hasProperty(Contact::endTime, null)
    .hasProperty(Contact::alert, contact.alert)
    .hasProperty(Contact::sensitive, contact.sensitive)
    .hasProperty(Contact::description, null)
    .hasProperty(Contact::linkedContact, contact)
    .hasProperty(Contact::partitionAreaId, 0)
    .hasProperty(Contact::staffEmployeeId, 1)
    .hasProperty(Contact::teamProviderId, 1)

  private fun havingDependentEntities(
    havingEvent: Boolean = true,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
  ) {
    val offenderId = Fake.id()

    requirement = Fake.requirement().apply { this.offenderId = offenderId }
    val disposals = listOf(Fake.disposal().apply { requirements = listOf(requirement, Fake.requirement()) })
    event = Fake.event().apply { this.disposals = disposals }
    offender = Fake.offender().apply {
      id = offenderId
      events = if (havingEvent) listOf(event, Fake.event()) else listOf()
    }

    outcome = Fake.contactOutcomeType()
    enforcement = Fake.enforcement()
    type = Fake.contactType()

    this.staff = Fake.staff()
    val staff = if (havingStaff) mutableListOf(this.staff, Fake.staff()) else listOf()

    officeLocation = Fake.officeLocation()
    team = Fake.team().apply { staff.map(this::addStaff) }
    provider = Fake.provider().apply { if (havingTeam) teams.addAll(listOf(team, Fake.team())) }

    nsi = Fake.nsi()
  }

  private fun whenCreatingSystemContactShouldSuccessfullySaveContact() {
    val savedContact = Fake.contact()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(savedContact)

    subject.createSystemContact(request)

    assertThat(entityCaptor.value)
      .hasProperty(Contact::offender, offender)
      .hasProperty(Contact::type, type)
      .hasProperty(Contact::outcome, null)
      .hasProperty(Contact::provider, provider)
      .hasProperty(Contact::team, team)
      .hasProperty(Contact::staff, staff)
      .hasProperty(Contact::event, event)
      .hasProperty(Contact::requirement, null)
      .hasProperty(Contact::officeLocation, null)
      .hasProperty(Contact::date, request.date)
      .hasProperty(Contact::startTime, request.startTime)
      .hasProperty(Contact::endTime, null)
      .hasProperty(Contact::alert, false)
      .hasProperty(Contact::sensitive, false)
      .hasProperty(Contact::notes, type.defaultHeadings)
      .hasProperty(Contact::description, null)
      .hasProperty(Contact::partitionAreaId, 0)
      .hasProperty(Contact::staffEmployeeId, 1)
      .hasProperty(Contact::teamProviderId, 1)
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingTypeById: Boolean? = true,
    havingTypeByCode: Boolean? = null,
    havingProvider: Boolean = true,
    havingNsi: Boolean = true,
  ) {
    request = Fake.newSystemContact().copy(
      typeId = if (havingTypeById == null) null else type.id,
      type = if (havingTypeByCode == null) null
      else Fake.faker.options().option(WellKnownContactType::class.java),
      offenderId = offender.id,
      nsiId = if (havingNsi) nsi.id else null,
      eventId = event.id,
      providerId = provider.id,
      teamId = team.id,
      staffId = staff.id,
    )

    whenever(offenderRepository.findById(offender.id))
      .thenReturn(if (havingOffender) Optional.of(offender) else Optional.empty())

    when (havingTypeById) {
      true -> whenever(contactTypeRepository.findById(type.id)).thenReturn(Optional.of(type))
      false -> whenever(contactTypeRepository.findById(type.id)).thenReturn(Optional.empty())
    }

    when (havingTypeByCode) {
      true -> whenever(contactTypeRepository.findByCode(request.type?.code!!)).thenReturn(type)
      false -> whenever(contactTypeRepository.findByCode(request.type?.code!!)).thenReturn(null)
    }

    whenever(providerRepository.findById(provider.id))
      .thenReturn(if (havingProvider) Optional.of(provider) else Optional.empty())

    whenever(nsiRepository.findById(nsi.id))
      .thenReturn(if (havingNsi) Optional.of(nsi) else Optional.empty())
  }

  private fun whenCreatingSystemContactShouldThrowIllegalArgumentException() {
    assertThrows<IllegalArgumentException> {
      subject.createSystemContact(request)
    }
  }
}
