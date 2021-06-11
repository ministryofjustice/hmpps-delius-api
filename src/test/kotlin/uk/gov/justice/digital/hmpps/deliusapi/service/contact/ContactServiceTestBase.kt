package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
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
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.security.SecurityUserContext
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
abstract class ContactServiceTestBase {
  @Mock protected lateinit var contactRepository: ContactRepository
  @Mock protected lateinit var offenderRepository: OffenderRepository
  @Mock protected lateinit var contactTypeRepository: ContactTypeRepository
  @Mock protected lateinit var providerRepository: ProviderRepository
  @Mock protected lateinit var nsiRepository: NsiRepository
  @Mock protected lateinit var mapper: ContactMapper
  @Mock protected lateinit var validationService: ContactValidationService
  @Mock protected lateinit var systemContactService: SystemContactService
  @Mock protected lateinit var contactBreachService: ContactBreachService
  @Mock protected lateinit var contactEnforcementService: ContactEnforcementService
  @Mock protected lateinit var contactRarService: ContactRarService
  @Mock protected lateinit var securityUserContext: SecurityUserContext
  @InjectMocks protected lateinit var subject: ContactService

  protected lateinit var type: ContactType
  protected lateinit var outcome: ContactOutcomeType
  protected lateinit var enforcement: Enforcement
  protected lateinit var offender: Offender
  protected lateinit var provider: Provider
  protected lateinit var staff: Staff
  protected lateinit var team: Team
  protected lateinit var officeLocation: OfficeLocation
  protected lateinit var event: Event
  protected lateinit var requirement: Requirement
  protected lateinit var nsi: Nsi

  protected fun havingDependentEntities(
    havingEvent: Boolean = true,
    havingRequirement: Boolean = true,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
    havingEnforcement: Boolean = true
  ) {
    val offenderId = Fake.id()

    requirement = Fake.requirement()
    requirement.offenderId = offenderId
    val disposal = Fake.disposal().apply {
      this.requirements = if (havingRequirement) listOf(requirement, Fake.requirement()) else listOf()
    }
    event = Fake.event().apply { disposals = listOf(disposal) }

    offender = Fake.offender().apply {
      id = offenderId
      events = if (havingEvent) listOf(event, Fake.event()) else listOf()
    }

    outcome = Fake.contactOutcomeType()

    if (havingEnforcement) {
      enforcement = Fake.enforcement()
    }

    type = Fake.contactType()

    this.staff = Fake.staff()
    val staffs = if (havingStaff) listOf(this.staff, Fake.staff()) else listOf()

    officeLocation = Fake.officeLocation()
    team = Fake.team().apply { staffs.map(this::addStaff) }
    provider = Fake.provider().apply { if (havingTeam) teams.addAll(listOf(team, Fake.team())) }

    nsi = Fake.nsi(event)
  }

  protected fun shouldAssertProviderAuthority(code: String) =
    verify(securityUserContext, times(1)).assertProviderAuthority(code)
}
