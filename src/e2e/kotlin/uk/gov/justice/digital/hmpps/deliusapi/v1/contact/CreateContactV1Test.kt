package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EventRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalTime

class CreateContactV1Test @Autowired constructor(
  private val repository: ContactRepository,
  private val eventRepository: EventRepository,
  private val nsiRepository: NsiRepository,
  private val requirementRepository: RequirementRepository
) : EndToEndTest() {

  private lateinit var request: NewContact
  private lateinit var response: ContactDto
  private lateinit var created: Contact

  private var requirementRarCount: Long = 0
  private var nsiRarCount: Long = 0

  @Test
  fun `Creating contact`() {
    request = configuration.newContact(ContactTestsConfiguration::updatable).copy(
      startTime = "09:00",
      endTime = "09:09"
    )
    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact against nsi & event`() {
    val nsi = havingExistingNsi(NsiTestsConfiguration::active)
    request = configuration.newContact(ContactTestsConfiguration::nsi).copy(
      nsiId = nsi.id,
      startTime = "09:10",
      endTime = "09:19"
    )
    ensureNoConflicts()
    gettingNsiRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementNsiRarCount(0)
  }

  @Test
  fun `Creating contact against nsi only`() {
    try {
      val nsi = havingExistingNsi(NsiTestsConfiguration::active)
      request = configuration.newContact(ContactTestsConfiguration::nsiOnly).copy(
        nsiId = nsi.id,
        startTime = "09:20",
        endTime = "09:29"
      )
      ensureNoConflicts()
      gettingNsiRarCount()
      whenCreatingContact()
      shouldCreateContact()
      shouldIncrementNsiRarCount(0)
    } finally {
      withDatabase {
        repository.deleteAllByNsiIdAndDate(request.nsiId!!, request.date)
      }
    }
  }

  @Test
  fun `Creating contact against RAR NSI when RAR activity`() {
    val nsi = havingExistingNsi(NsiTestsConfiguration::rar)
    request = configuration.newContact(ContactTestsConfiguration::rarNsi).copy(
      rarActivity = true,
      nsiId = nsi.id,
      startTime = "09:30",
      endTime = "09:39"
    )

    ensureNoConflicts()
    gettingNsiRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementNsiRarCount(1)
  }

  @Test
  fun `Creating contact against RAR NSI when not RAR activity`() {
    val nsi = havingExistingNsi(NsiTestsConfiguration::rar)
    request = configuration.newContact(ContactTestsConfiguration::rarNsi).copy(
      rarActivity = false,
      nsiId = nsi.id,
      startTime = "09:40",
      endTime = "09:49"
    )

    ensureNoConflicts()
    gettingNsiRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementNsiRarCount(0)
  }

  @Test
  fun `Creating contact against event`() {
    request = configuration.newContact(ContactTestsConfiguration::event).copy(
      startTime = "09:50",
      endTime = "09:59"
    )
    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating non-RAR contact against requirement`() {
    request = configuration.newContact(ContactTestsConfiguration::requirement)
    ensureNoConflicts()
    gettingRequirementRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementRequirementRarCount(0)
  }

  @Test
  fun `Creating RAR activity contact against RAR requirement`() {
    request = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
      rarActivity = true,
      startTime = "10:00",
      endTime = "10:09"
    )
    ensureNoConflicts()
    gettingRequirementRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementRequirementRarCount(1)
  }

  @Test
  fun `Creating non-RAR activity contact against RAR requirement`() {
    request = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
      rarActivity = false,
      startTime = "10:10",
      endTime = "10:19"
    )
    ensureNoConflicts()
    gettingRequirementRarCount()
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementRequirementRarCount(0)
  }

  @Test
  fun `Creating contact with enforcement`() {
    request = configuration.newContact(ContactTestsConfiguration::enforcement).copy(
      startTime = "10:20",
      endTime = "10:29"
    )
    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating appointment contact on contact type that does not support RAR`() {
    request = configuration.newContact(ContactTestsConfiguration::appointment).copy(
      startTime = "10:30",
      endTime = "10:39"
    )

    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating breach start contact`() {
    request = configuration.newContact(ContactTestsConfiguration::breachStart).copy(
      startTime = "10:40",
      endTime = "10:49"
    )

    havingEvent {
      it.inBreach = false
      it.breachEnd = null
    }

    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()

    shouldUpdateEvent {
      assertThat(it)
        .hasProperty(Event::inBreach, true)
    }
  }

  @Test
  fun `Creating ftc contact`() {
    request = configuration.newContact(ContactTestsConfiguration::ftc)

    var limit = 0L
    havingEvent {
      it.ftcCount = 0
      it.inBreach = false
      it.breachEnd = null
      limit = it.disposal?.type?.failureToComplyLimit
        ?: throw RuntimeException("Event with id '${it.id}' has a sentence without an ftc limit")
    }

    if (limit == 0L) {
      throw RuntimeException("Event with id '${request.eventId}' has a sentence with a 0 ftc limit")
    }

    withDatabase {
      repository.deleteAllByEventIdAndTypeNationalStandardsContactIsTrue(request.eventId!!)
      repository.deleteAllByEventIdAndTypeCode(request.eventId!!, WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code)
    }

    logger.info("event has ftc limit $limit, first create ${limit - 1} ftc contacts to push the ftc count just below the limit")
    request = request.copy(date = LocalDate.now().minusDays(limit - 1))
    for (i in 1 until limit) {
      logger.info("creating pre ftc limit contact no. $i")
      whenCreatingContact()
      shouldCreateContact()

      shouldUpdateEvent {
        assertThat(it)
          .hasProperty(Event::ftcCount, i)
      }

      shouldSaveReviewEnforcementContact(false)

      request = request.copy(date = request.date.plusDays(1))
    }

    logger.info("creating ftc limit breach contact")
    whenCreatingContact()
    shouldCreateContact()

    shouldUpdateEvent {
      assertThat(it)
        .hasProperty(Event::ftcCount, limit)
    }

    shouldSaveReviewEnforcementContact()
  }

  private fun shouldSaveReviewEnforcementContact(should: Boolean = true) = withDatabase {
    val reviewContacts = repository.findAllByEventIdAndTypeCode(
      request.eventId!!,
      WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code
    )
    assertThat(reviewContacts)
      .describedAs("should save review enforcement contact")
      .hasSize(if (should) 1 else 0)
  }

  private fun shouldUpdateEvent(assert: (event: Event) -> Unit) = withDatabase {
    val event = eventRepository.findByIdOrNull(request.eventId!!)
      ?: throw RuntimeException("cannot find event with id '${request.eventId}'")
    assert(event)
  }

  private fun havingEvent(mutate: (event: Event) -> Unit) = withDatabase {
    val event = eventRepository.findByIdOrNull(request.eventId!!)
      ?: throw RuntimeException("cannot find event with id '${request.eventId}'")
    mutate(event)
    eventRepository.saveAndFlush(event)
  }

  private fun whenCreatingContact() {
    response = contactV1.safely { it.createContact(request) }
  }

  private fun shouldCreateContact() = withDatabase {
    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

    val observed = NewContact(
      date = created.date,
      offenderCrn = created.offender.crn,
      provider = created.provider!!.code,
      staff = created.staff!!.code,
      team = created.team!!.code,
      officeLocation = created.officeLocation?.code,
      startTime = created.startTime.toString(),
      endTime = created.endTime.toString(),
      type = created.type.code,
      alert = created.alert,
      sensitive = created.sensitive,
      description = created.description,
      outcome = created.outcome?.code,
      enforcement = created.enforcement?.action?.code,
      eventId = created.event?.id,
      requirementId = created.requirement?.id,
      nsiId = created.nsi?.id,
      notes = created.notes,
      rarActivity = created.rarActivity
    )

    assertThat(observed)
      .describedAs("contact with id '${created.id}' should be saved")
      .usingRecursiveComparison()
      .ignoringCollectionOrder()
      .ignoringFields("alert", "eventId") // TODO determine why this field is always null on insert into test... triggers?
      .isEqualTo(request)
  }

  private fun gettingNsiRarCount() = withDatabase {
    nsiRarCount = repository.countNsiRar(request.nsiId!!)
  }

  private fun shouldIncrementNsiRarCount(increment: Long) = withDatabase {
    val actualRarCount = nsiRepository.getOne(request.nsiId!!).rarCount ?: 0
    val expectedRarCount = nsiRarCount + increment
    assertThat(actualRarCount).isEqualTo(expectedRarCount)
  }

  private fun gettingRequirementRarCount() = withDatabase {
    requirementRarCount = repository.countRequirementRar(request.requirementId!!)
  }

  private fun shouldIncrementRequirementRarCount(increment: Long) = withDatabase {
    val actualRarCount = requirementRepository.getOne(request.requirementId!!).rarCount ?: 0
    val expectedRarCount = requirementRarCount + increment
    assertThat(actualRarCount).isEqualTo(expectedRarCount)
  }

  private fun ensureNoConflicts() {
    withDatabase {
      repository.deleteAllByOffenderCrnAndDateAndStartTimeAndEndTime(request.offenderCrn, request.date, LocalTime.parse(request.startTime), LocalTime.parse(request.endTime))
    }
  }
}
