package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Disposal
import uk.gov.justice.digital.hmpps.deliusapi.entity.DisposalType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Enforcement
import uk.gov.justice.digital.hmpps.deliusapi.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.ReferenceDataMaster
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.RequirementTypeCategory
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.TransferReason
import uk.gov.justice.digital.hmpps.deliusapi.entity.User
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.Y
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit

object Fake {
  val faker = Faker()
  val contactMapper: ContactMapper = ContactMapper.INSTANCE
  val nsiMapper: NsiMapper = NsiMapper.INSTANCE

  const val ALLOWED_CONTACT_TYPES = "TST01,TST02,TST03"
  private val allowedContactTypes = ALLOWED_CONTACT_TYPES.split(',').toTypedArray()

  private fun Date.toLocalTime(): LocalTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
  private fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  private fun Date.toLocalDateTime(): LocalDateTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

  fun localTimeBetween(hourFrom: Int, hourTo: Int): LocalTime = faker.date().between(
    Date.from(LocalDateTime.of(1900, 1, 1, hourFrom, 0).toInstant(ZoneOffset.UTC)),
    Date.from(LocalDateTime.of(1900, 1, 1, hourTo, 0).toInstant(ZoneOffset.UTC))
  ).toLocalTime()

  fun randomPastLocalDate(): LocalDate = faker.date().past(10, 1, TimeUnit.DAYS).toLocalDate()
  fun randomLocalDateTime(): LocalDateTime = faker.date().past(10, 1, TimeUnit.DAYS).toLocalDateTime()
  fun randomFutureLocalDate(): LocalDate = faker.date().future(10, 1, TimeUnit.DAYS).toLocalDate()

  fun id(): Long = faker.number().numberBetween(1L, 900_000_000_000_000_000L) // maxvalue of db sequences
  fun count(): Long = faker.number().numberBetween(1L, 100L)

  private fun crn() = "${faker.lorem().fixedString(1)}${faker.number().randomNumber(6, true)}"

  fun offender() = Offender(id = id(), crn = crn(), events = listOf(event()))

  fun contactType() = ContactType(
    id = id(),
    code = faker.options().option(*allowedContactTypes),
    selectable = true,
    spgOverride = false,
    alertFlag = true,
    outcomeFlag = Y,
    locationFlag = Y,
    attendanceContact = true,
    recordedHoursCredited = true,
    cjaOrderLevel = true,
    legacyOrderLevel = true,
    offenderLevel = true,
    wholeOrderLevel = true,
    scheduleFutureAppointments = true,
    editable = true,
    defaultHeadings = faker.company().bs(),
    outcomeTypes = listOf(contactOutcomeType()),
    requirementTypeCategories = listOf(requirementTypeCategory()),
    nsiTypes = listOf(nsiType()),
    nationalStandardsContact = true,
  )
  fun contactOutcomeType() = ContactOutcomeType(
    id = id(),
    code = faker.lorem().characters(1, 10),
    compliantAcceptable = true,
    attendance = true,
    actionRequired = true,
    enforceable = true,
  )
  fun provider() = Provider(id = id(), code = faker.lorem().characters(3), teams = listOf(team()))
  fun team() = Team(
    id = id(),
    code = faker.bothify("?##?##"),
    staff = listOf(staff()),
    officeLocations = listOf(officeLocation())
  )
  fun officeLocation() = OfficeLocation(id = id(), code = faker.lorem().characters(7))
  fun staff() = Staff(id = id(), code = faker.bothify("?##?###"))

  fun requirementTypeCategory() = RequirementTypeCategory(
    id = id(),
    code = faker.lorem().characters(1, 20),
    nsiTypes = listOf(nsiType())
  )
  fun requirement() = Requirement(
    id = id(),
    offenderId = id(),
    active = true,
    typeCategory = requirementTypeCategory(),
    terminationDate = null,
  )

  fun disposalType() = DisposalType(
    id = id(),
    cja2003Order = true,
    legacyOrder = true,
    sentenceType = faker.lorem().characters(5),
    failureToComplyLimit = count(),
  )

  fun disposal() = Disposal(
    id = id(),
    requirements = listOf(requirement()),
    type = disposalType(),
    date = randomPastLocalDate(),
  )

  fun event() = Event(
    id = id(),
    disposals = listOf(disposal()),
    referralDate = randomPastLocalDate(),
    active = true,
    ftcCount = count(),
    inBreach = true,
  )

  fun enforcementAction() = EnforcementAction(
    id = id(),
    code = faker.lorem().characters(10),
    description = faker.company().bs(),
    outstandingContactAction = true,
    responseByPeriod = 7,
    contactType = contactType(),
  )

  fun enforcement() = Enforcement(
    id = id(),
    responseDate = randomPastLocalDate(),
    actionTakenDate = randomPastLocalDate(),
    actionTakenTime = LocalTime.NOON,
    action = enforcementAction(),
  )

  fun contact(): Contact {
    val contactOutcomeType = contactOutcomeType()
    val team = team()
    val provider = provider().apply { teams = listOf(team) }
    return Contact(
      id = id(),
      offender = offender(),
      type = contactType().apply { outcomeTypes = listOf(contactOutcomeType) },
      outcome = contactOutcomeType,
      enforcements = mutableListOf(enforcement()),
      provider = provider,
      team = team,
      staff = staff(),
      officeLocation = officeLocation(),
      date = randomPastLocalDate(),
      startTime = localTimeBetween(0, 12),
      endTime = localTimeBetween(12, 23),
      alert = faker.bool().bool(),
      sensitive = faker.bool().bool(),
      notes = faker.lorem().paragraph(),
      partitionAreaId = id(),
      staffEmployeeId = id(),
      teamProviderId = id(),
      description = faker.company().bs(),
      event = event(),
      requirement = requirement(),
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      createdByUserId = id(),
      lastUpdatedUserId = id(),
      attended = faker.bool().bool(),
      complied = faker.bool().bool(),
      hoursCredited = faker.number().randomDouble(1, 1, 12),
    )
  }

  fun contactDto(): ContactDto = contactMapper.toDto(contact())

  fun newContact(): NewContact = contactMapper.toNew(contactDto())

  /**
   * A request to create a new contact that will succeed against the test data.
   */
  fun validNewContact() = NewContact(
    offenderCrn = "X320741",
    type = "EAP0", // AP Register - INCIDENT
    outcome = "CO22", // No Action Required
    nsiId = null,
    provider = "C00",
    team = "C00T01",
    staff = "C00T01U",
    officeLocation = "C00OFFA",
    alert = false,
    eventId = 2500295343,
    requirementId = 2500083652,
    date = randomPastLocalDate(),
    startTime = localTimeBetween(0, 12),
    endTime = localTimeBetween(12, 23),
    sensitive = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    description = faker.company().bs(),
  )

  fun updateContact(): UpdateContact = contactMapper.toUpdate(contact())
    .copy(notes = faker.company().bs())

  fun newSystemContact() = NewSystemContact(
    typeId = id(),
    offenderId = id(),
    nsiId = id(),
    eventId = id(),
    providerId = id(),
    teamId = id(),
    staffId = id(),
    timestamp = randomLocalDateTime()
  )

  fun auditedInteraction() = AuditedInteraction(
    dateTime = randomLocalDateTime(),
    success = faker.bool().bool(),
    parameters = faker.lorem().characters(),
    businessInteraction = businessInteraction(),
    userId = id(),
  )

  fun businessInteraction() = BusinessInteraction(
    id = id(),
    code = faker.letterify("????"),
    description = faker.lorem().characters(50),
    enabledDate = randomLocalDateTime()
  )

  fun nsiType() = NsiType(
    id = id(),
    code = faker.lorem().characters(1, 20),
    offenderLevel = true,
    eventLevel = true,
    allowActiveDuplicates = true,
    allowInactiveDuplicates = true,
    units = standardReference(),
    minimumLength = faker.number().numberBetween(1L, 25L),
    maximumLength = faker.number().numberBetween(75L, 100L),
  )

  fun standardReference() = StandardReference(
    id = id(),
    code = faker.lorem().characters(1, 20),
    description = faker.company().bs(),
  )

  fun nsiStatus() = NsiStatus(
    id = id(),
    code = faker.lorem().characters(1, 20),
    contactTypeId = id(),
  )

  fun nsiManager(nsi: Nsi = nsi()): NsiManager {
    val staff = staff()
    val team = team().apply { this.staff = listOf(staff) }
    val provider = provider().apply { teams = listOf(team) }
    return NsiManager(
      id = id(),
      nsi = nsi,
      startDate = randomPastLocalDate(),
      provider = provider,
      team = team,
      staff = staff,
      active = true,
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      createdByUserId = id(),
      lastUpdatedUserId = id(),
    )
  }

  fun nsiManagerDto(): NsiManagerDto = nsiMapper.toDto(nsiManager())

  fun newNsiManager(): NewNsiManager = nsiMapper.toNew(nsiManagerDto())

  fun nsi(): Nsi {
    val nsi = Nsi(
      id = id(),
      offender = offender(),
      event = event(),
      type = nsiType(),
      subType = standardReference(),
      length = faker.number().numberBetween(25L, 75L),
      referralDate = faker.date().past(100, 20, TimeUnit.DAYS).toLocalDate(),
      expectedStartDate = randomPastLocalDate(),
      expectedEndDate = randomFutureLocalDate(),
      startDate = randomPastLocalDate(),
      endDate = LocalDate.now(),
      status = nsiStatus(),
      statusDate = randomLocalDateTime(),
      notes = faker.lorem().paragraph(),
      outcome = standardReference(),
      active = false, // end date is provided here
      pendingTransfer = false,
      requirement = requirement(),
      intendedProvider = provider(),
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      createdByUserId = id(),
      lastUpdatedUserId = id(),
    )

    nsi.managers.add(nsiManager(nsi))

    return nsi
  }

  fun nsiDto(): NsiDto = nsiMapper.toDto(nsi())

  fun newNsi(): NewNsi = nsiMapper.toNew(nsiDto())

  fun user(): User = User(
    id = id(),
    distinguishedName = faker.esports().player(),
    providers = listOf(provider())
  )

  fun transferReason() = TransferReason(
    id = id(),
    code = faker.lorem().characters(1, 20),
  )

  fun referenceDataMaster() = ReferenceDataMaster(
    id = id(),
    code = faker.lorem().characters(1, 20),
    standardReferences = listOf(standardReference(), standardReference()),
  )
}
