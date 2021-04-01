package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.TransferReason
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiManagerRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ReferenceDataMasterRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TransferReasonRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.SystemContactService
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
abstract class NsiManagerServiceTest {
  @Mock protected lateinit var repository: NsiManagerRepository
  @Mock protected lateinit var providerRepository: ProviderRepository
  @Mock protected lateinit var transferReasonRepository: TransferReasonRepository
  @Mock protected lateinit var referenceDataMasterRepository: ReferenceDataMasterRepository
  @Mock protected lateinit var systemContactService: SystemContactService
  @Captor protected lateinit var newSystemContactCaptor: ArgumentCaptor<NewSystemContact>
  @InjectMocks protected lateinit var subject: NsiManagerService

  protected lateinit var provider: Provider
  protected lateinit var team: Team
  protected lateinit var staff: Staff
  protected lateinit var allocationReason: StandardReference
  protected lateinit var transferReason: TransferReason

  protected fun havingProvider(
    having: Boolean? = null,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
  ) {
    staff = Fake.staff()
    team = Fake.team().also {
      if (havingStaff) it.addStaff(staff)
    }
    provider = Fake.provider().apply {
      if (havingTeam) teams.add(team)
    }

    if (having != null) {
      whenever(providerRepository.findByCodeAndSelectableIsTrue(provider.code))
        .thenReturn(if (having) provider else null)
    }
  }

  protected fun havingAllocationReason(
    code: String,
    havingDataset: Boolean = true,
    havingReference: Boolean = true,
  ) {
    allocationReason = Fake.standardReference().also { it.code = code }
    val dataset = Fake.referenceDataMaster().apply {
      standardReferences = if (havingReference) listOf(allocationReason) else emptyList()
    }
    whenever(referenceDataMasterRepository.findByCode(WellKnownReferenceDataset.NSI_MANAGER_ALLOCATION_REASON.code))
      .thenReturn(if (havingDataset) dataset else null)
  }

  protected fun havingNsiTransferReason(having: Boolean = true) {
    transferReason = Fake.transferReason()
    whenever(transferReasonRepository.findByCode(WellKnownTransferReason.NSI.code))
      .thenReturn(if (having) transferReason else null)
  }

  protected fun havingNewSystemContactCaptor() =
    doNothing().whenever(systemContactService).createSystemContact(capture(newSystemContactCaptor))
}

class CreateNsiManagerTest : NsiManagerServiceTest() {
  private lateinit var nsi: Nsi
  private lateinit var request: NewNsiManager
  private lateinit var startDate: LocalDate

  @Test
  fun `Attempting to create nsi manager but missing provider`() {
    havingProvider(false)
    havingRequest()
    assertThrows<BadRequestException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Attempting to create nsi manager but missing team`() {
    havingProvider(true, havingTeam = false)
    havingRequest()
    assertThrows<BadRequestException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Attempting to create nsi manager but missing staff`() {
    havingProvider(true, havingStaff = false)
    havingRequest()
    assertThrows<BadRequestException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Attempting to create nsi manager but missing allocation reason dataset`() {
    havingProvider(true)
    havingAllocationReason("IN1", havingDataset = false)
    havingRequest()
    assertThrows<RuntimeException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Attempting to create nsi manager but missing allocation reason`() {
    havingProvider(true)
    havingAllocationReason("IN1", havingReference = false)
    havingRequest()
    assertThrows<RuntimeException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Attempting to create nsi manager but missing transfer reason`() {
    havingProvider(true)
    havingAllocationReason("IN1")
    havingNsiTransferReason(false)
    havingRequest()
    assertThrows<RuntimeException> { whenCreatingNsiManager() }
    shouldNotSetNsiManager()
  }

  @Test
  fun `Successfully creating nsi manager`() {
    havingProvider(true)
    havingAllocationReason("IN1")
    havingNsiTransferReason()
    havingRequest()
    whenCreatingNsiManager()
    shouldSetNsiManager()
  }

  private fun havingRequest() {
    nsi = Fake.nsi().apply { manager = null }
    request = NewNsiManager(provider = provider.code, team = team.code, staff = staff.code)
    startDate = LocalDate.of(2021, 3, 26)
  }

  private fun whenCreatingNsiManager() = subject.createNsiManager(nsi, request, startDate)

  private fun shouldNotSetNsiManager() = assertThat(nsi.manager).isEqualTo(null)

  private fun shouldSetNsiManager() =
    assertThat(nsi.manager)
      .isNotNull
      .extractingObject { it!! }
      .hasProperty(NsiManager::nsi, nsi)
      .hasProperty(NsiManager::startDate, startDate)
      .hasProperty(NsiManager::provider, provider)
      .hasProperty(NsiManager::team, team)
      .hasProperty(NsiManager::staff, staff)
      .hasProperty(NsiManager::transferReason, transferReason)
      .hasProperty(NsiManager::allocationReason, allocationReason)
}

class UpdateNsiManagerTest : NsiManagerServiceTest() {
  private lateinit var nsi: Nsi
  private lateinit var oldManager: NsiManager
  private lateinit var request: UpdateNsiManager

  @Test
  fun `Attempting to update nsi manager but no existing manager`() {
    havingProvider()
    havingRequest()
    nsi.manager = null
    assertThrows<RuntimeException> { whenUpdatingNsiManager() }
    assertThat(nsi.manager).isEqualTo(null)
  }

  @Test
  fun `Attempting to update nsi manager but no existing provider`() {
    havingProvider()
    havingRequest()
    nsi.manager!!.provider = null
    assertThrows<RuntimeException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but missing team`() {
    havingProvider(havingTeam = false)
    havingRequest()
    assertThrows<BadRequestException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but missing staff`() {
    havingProvider(havingStaff = false)
    havingRequest()
    assertThrows<BadRequestException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but no transfer requested`() {
    havingProvider()
    havingRequest(havingTransfer = false)
    whenUpdatingNsiManager()
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but no transfer reason provided`() {
    havingProvider()
    havingRequest(havingTransferReason = false)
    assertThrows<BadRequestException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but no transfer date provided`() {
    havingProvider()
    havingRequest(havingTransferDate = false)
    assertThrows<BadRequestException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but missing allocation reason dataset`() {
    havingProvider()
    havingAllocationReason("ALLOCATION_REASON", havingDataset = false)
    havingRequest()
    assertThrows<RuntimeException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but missing allocation reason`() {
    havingProvider()
    havingAllocationReason("ALLOCATION_REASON", havingReference = false)
    havingRequest()
    assertThrows<BadRequestException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Attempting to update nsi manager but missing transfer reason`() {
    havingProvider()
    havingAllocationReason("ALLOCATION_REASON")
    havingNsiTransferReason(false)
    havingRequest()
    assertThrows<RuntimeException> { whenUpdatingNsiManager() }
    shouldNotSaveAnything()
  }

  @Test
  fun `Successfully updating nsi manager`() {
    havingProvider()
    havingAllocationReason("ALLOCATION_REASON")
    havingNsiTransferReason()
    havingRequest()
    havingNewSystemContactCaptor()
    whenUpdatingNsiManager()
    shouldRemovePreviousManager()
    shouldSetNsiManager()
    shouldCreateTransferContact()
  }

  private fun havingRequest(
    havingTransfer: Boolean = true,
    havingTransferDate: Boolean = true,
    havingTransferReason: Boolean = true,
  ) {
    nsi = Fake.nsi()
    oldManager = nsi.manager!!
    oldManager.provider = provider
    provider.teams.addAll(provider.teams + oldManager.team!!)
    team.addStaff(oldManager.staff!!)

    request = UpdateNsiManager(
      team = if (havingTransfer) team.code else nsi.manager?.team?.code,
      staff = if (havingTransfer) staff.code else nsi.manager?.staff?.code,
      transferDate = if (havingTransferDate) LocalDate.of(2021, 3, 26) else null,
      transferReason = if (havingTransferReason) "ALLOCATION_REASON" else null,
    )
  }

  private fun whenUpdatingNsiManager() = subject.updateNsiManager(nsi, request)

  private fun shouldNotSaveAnything() {
    assertThat(nsi.manager).isEqualTo(oldManager)
    verify(repository, never()).saveAndFlush(any())
    verify(systemContactService, never()).createSystemContact(any())
  }

  private fun shouldRemovePreviousManager() {
    assertThat(oldManager)
      .hasProperty(NsiManager::endDate, request.transferDate)
      .hasProperty(NsiManager::active, false)
    verify(repository, times(1)).saveAndFlush(oldManager)
  }

  private fun shouldSetNsiManager() =
    assertThat(nsi.manager)
      .isNotNull
      .extractingObject { it!! }
      .hasProperty(NsiManager::nsi, nsi)
      .hasProperty(NsiManager::startDate, request.transferDate)
      .hasProperty(NsiManager::provider, provider)
      .hasProperty(NsiManager::team, team)
      .hasProperty(NsiManager::staff, staff)
      .hasProperty(NsiManager::transferReason, transferReason)
      .hasProperty(NsiManager::allocationReason, allocationReason)

  private fun shouldCreateTransferContact() {
    assertThat(newSystemContactCaptor.value)
      .usingRecursiveComparison()
      .isEqualTo(
        NewSystemContact(
          type = WellKnownContactType.NSI_TRANSFER,
          offenderId = nsi.offender?.id!!,
          date = request.transferDate!!,
          eventId = nsi.event?.id,
          nsiId = nsi.id,
          teamId = team.id,
          staffId = staff.id,
          providerId = provider.id,
          notes = """
          Transfer Reason: Internal Transfer
          Transfer Date: 26/03/2021
          From Trust: ${provider.description}
          From Team: ${oldManager.team!!.description}
          From Officer: ${oldManager.staff!!.firstName} ${oldManager.staff!!.middleName} ${oldManager.staff!!.lastName}
          -------------------------------
          
          """.trimIndent()
        )
      )
  }
}
