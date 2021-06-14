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
    try {
      val nsi = havingExistingNsi(NsiTestsConfiguration::rar)
      request = configuration.newContact(ContactTestsConfiguration::rarNsi).copy(
        rarActivity = true,
        nsiId = nsi.id,
        startTime = "09:30",
        endTime = "09:39"
      )

      ensureNoConflicts()
      gettingNsiRarCount()
      gettingRequirementRarCount(nsi.requirementId!!)
      whenCreatingContact()
      shouldCreateContact()
      shouldIncrementNsiRarCount(1)
      shouldIncrementRequirementRarCount(0, nsi.requirementId!!)
    } finally {
      ensureNoConflicts() // delete appointment so another appointment on the same day still increments counter
    }
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
    gettingRequirementRarCount(nsi.requirementId!!)
    whenCreatingContact()
    shouldCreateContact()
    shouldIncrementNsiRarCount(0)
    shouldIncrementRequirementRarCount(0, nsi.requirementId!!)
  }

  @Test
  fun `Creating 2 RAR NSI contacts on the same day only increments the RAR count once`() {
    var firstContact: NewContact? = null
    var secondContact: NewContact? = null
    try {
      val nsi = havingExistingNsi(NsiTestsConfiguration::rar)
      firstContact = configuration.newContact(ContactTestsConfiguration::rarNsi).copy(
        rarActivity = true,
        nsiId = nsi.id,
        startTime = "09:50",
        endTime = "09:59"
      )
      request = firstContact

      ensureNoConflicts()
      gettingNsiRarCount()
      gettingRequirementRarCount(nsi.requirementId!!)

      whenCreatingContact()
      shouldCreateContact()

      shouldIncrementNsiRarCount(1)
      shouldIncrementRequirementRarCount(0, nsi.requirementId!!)

      secondContact = configuration.newContact(ContactTestsConfiguration::rarNsi).copy(
        rarActivity = true,
        nsiId = nsi.id,
        startTime = "10:00",
        endTime = "10:09"
      )
      request = secondContact

      whenCreatingContact()
      shouldCreateContact()

      // count has still only increased by 1
      shouldIncrementNsiRarCount(1)
      shouldIncrementRequirementRarCount(0, nsi.requirementId!!)
    } finally {
      // delete appointments so another appointment on the same day still increments counter
      if (firstContact != null) {
        ensureNoConflicts(firstContact)
      }
      if (secondContact != null) {
        ensureNoConflicts(secondContact)
      }
    }
  }

  @Test
  fun `Creating contact against event`() {
    request = configuration.newContact(ContactTestsConfiguration::event).copy(
      startTime = "10:10",
      endTime = "10:19"
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
    try {
      request = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
        rarActivity = true,
        startTime = "10:20",
        endTime = "10:29"
      )
      ensureNoConflicts()
      gettingRequirementRarCount()
      whenCreatingContact()
      shouldCreateContact()
      shouldIncrementRequirementRarCount(1)
    } finally {
      ensureNoConflicts() // delete appointment so another appointment on the same day still increments counter
    }
  }

  @Test
  fun `Creating 2 RAR requirement contacts on the same day only increments the RAR count once`() {
    var firstContact: NewContact? = null
    var secondContact: NewContact? = null
    try {
      firstContact = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
        rarActivity = true,
        startTime = "10:30",
        endTime = "10:39"
      )
      request = firstContact

      ensureNoConflicts()
      gettingRequirementRarCount()

      whenCreatingContact()
      shouldCreateContact()

      shouldIncrementRequirementRarCount(1)

      secondContact = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
        rarActivity = true,
        startTime = "10:40",
        endTime = "10:49"
      )
      request = secondContact

      whenCreatingContact()
      shouldCreateContact()

      // count has still only increased by 1
      shouldIncrementRequirementRarCount(1)
    } finally {
      // delete appointments so another appointment on the same day still increments counter
      if (firstContact != null) {
        ensureNoConflicts(firstContact)
      }
      if (secondContact != null) {
        ensureNoConflicts(secondContact)
      }
    }
  }

  @Test
  fun `Creating non-RAR activity contact against RAR requirement`() {
    request = configuration.newContact(ContactTestsConfiguration::rarRequirement).copy(
      rarActivity = false,
      startTime = "10:50",
      endTime = "10:59"
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
      startTime = "11:00",
      endTime = "11:09"
    )
    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact(enforcementExpected = true)
  }

  @Test
  fun `Creating appointment contact on contact type that does not support RAR`() {
    request = configuration.newContact(ContactTestsConfiguration::appointment).copy(
      startTime = "11:10",
      endTime = "11:19"
    )

    ensureNoConflicts()
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating breach start contact`() {
    request = configuration.newContact(ContactTestsConfiguration::breachStart).copy(
      startTime = "11:20",
      endTime = "11:29"
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
      shouldCreateContact(enforcementExpected = true)

      shouldUpdateEvent {
        assertThat(it)
          .hasProperty(Event::ftcCount, i)
      }

      shouldSaveReviewEnforcementContact(false)

      request = request.copy(date = request.date.plusDays(1))
    }

    logger.info("creating ftc limit breach contact")
    whenCreatingContact()
    shouldCreateContact(enforcementExpected = true)

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

  private fun shouldCreateContact(enforcementExpected: Boolean = false) = withDatabase {
    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

    // TODO: Is there a better way than converting back to the request type? We can't check fields not present in the request
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

    shouldRecordEnforcementFlags(enforcementExpected)
  }

  private fun gettingNsiRarCount() = withDatabase {
    nsiRarCount = repository.countNsiRar(request.nsiId!!)
    logger.info("Existing NSI RAR count for ${request.nsiId!!} is $nsiRarCount")
  }

  private fun shouldIncrementNsiRarCount(increment: Long) = withDatabase {
    val actualRarCount = nsiRepository.getOne(request.nsiId!!).rarCount ?: 0
    val expectedRarCount = nsiRarCount + increment
    assertThat(actualRarCount).isEqualTo(expectedRarCount)
  }

  private fun gettingRequirementRarCount(requirementId: Long = request.requirementId!!) = withDatabase {
    requirementRarCount = repository.countRequirementRar(requirementId)
    logger.info("Existing requirements RAR count for $requirementId is $requirementRarCount")
  }

  private fun shouldIncrementRequirementRarCount(increment: Long, requirementId: Long = request.requirementId!!) = withDatabase {
    val actualRarCount = requirementRepository.getOne(requirementId).rarCount ?: 0
    val expectedRarCount = requirementRarCount + increment
    assertThat(actualRarCount).isEqualTo(expectedRarCount)
  }

  private fun shouldRecordEnforcementFlags(enforcementExpected: Boolean) = withDatabase {
    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

    val expectedEnforcementActionId = when (enforcementExpected) {
      true -> created.enforcement?.action?.id
      else -> null
    }

    val expectedEnforcementDiary = when (enforcementExpected) {
      true -> true
      else -> null
    }

    assertThat(created.enforcementDiary).isEqualTo(expectedEnforcementDiary)
    assertThat(created.enforcementActionID).isEqualTo(expectedEnforcementActionId)

    if (enforcementExpected) {
      assertThat(created.enforcement).isNotNull
    } else {
      assertThat(created.enforcement).isNull()
    }
  }

  private fun ensureNoConflicts(contact: NewContact = request) {
    withDatabase {
      repository.deleteAllByOffenderCrnAndDateAndStartTimeAndEndTime(contact.offenderCrn, contact.date, LocalTime.parse(contact.startTime), LocalTime.parse(contact.endTime))
    }
  }
}
