package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Enforcement
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.ConflictException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.comparingDateTimesToNearestSecond
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ContactValidationServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @Mock private lateinit var enforcementActionRepository: EnforcementActionRepository
  @InjectMocks private lateinit var subject: ContactValidationService

  @Test
  fun `Successfully validating contact type`() =
    attemptingToValidateContactType(success = true)

  @Test
  fun `Attempting to validate non-alert contact type with alert flag set`() =
    attemptingToValidateContactType(success = false, alertFlag = false)

  @Test
  fun `Attempting to validate recorded hours contact type without end time`() =
    attemptingToValidateContactType(success = false, havingEndTime = false)

  @Test
  fun `Successfully validating outcome type`() =
    attemptingToValidateOutcomeType(success = true)

  @Test
  fun `Successfully validating required outcome type without outcome in future`() =
    attemptingToValidateOutcomeType(success = null, havingRequestOutcome = false, havingPastDate = false)

  @Test
  fun `Attempting to validate required outcome type without outcome`() =
    attemptingToValidateOutcomeType(success = false, havingRequestOutcome = false)

  @Test
  fun `Attempting to validate non-permissible absence outcome type in future`() =
    attemptingToValidateOutcomeType(success = false, isPermissibleAbsence = false, havingPastDate = false)

  @Test
  fun `Successfully validating non-permissible absence outcome type in past`() =
    attemptingToValidateOutcomeType(success = true, isPermissibleAbsence = false)

  @Test
  fun `Successfully validating office location`() =
    attemptingToValidateOfficeLocation(success = true)

  @Test
  fun `Attempting to validate required office location without office location`() =
    attemptingToValidateOfficeLocation(success = false, havingRequestOfficeLocation = false)

  @Test
  fun `Attempting to validate required office location with missing office location`() =
    attemptingToValidateOfficeLocation(success = false, havingOfficeLocation = false)

  @Test
  fun `Attempting to validate non-required office location with office location`() =
    attemptingToValidateOfficeLocation(success = false, locationFlag = YesNoBoth.N)

  @Test
  fun `Successfully validating non-required office location without office location`() =
    attemptingToValidateOfficeLocation(success = null, locationFlag = YesNoBoth.N, havingRequestOfficeLocation = false)

  @Test
  fun `Successfully validating required-or-non-required office location with office location`() =
    attemptingToValidateOfficeLocation(success = true, locationFlag = YesNoBoth.B)

  @Test
  fun `Successfully validating required-or-non-required office location without office location`() =
    attemptingToValidateOfficeLocation(success = null, locationFlag = YesNoBoth.B, havingRequestOfficeLocation = false)

  @Test
  fun `Successfully validating future appointment without clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingClashes = false)

  @Test
  fun `Successfully validating past appointment with clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingFutureDate = false)

  @Test
  fun `Successfully validating future appointment without end time`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingEndTime = false)

  @Test
  fun `Successfully validating non-attendance contact as future appointment`() =
    attemptingToValidateFutureAppointmentClashes(success = true, attendanceContact = false)

  @Test
  fun `Attempting to validate future appointment with clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = false, havingClashes = true)

  @Test
  fun `Successfully validating whole order, requirement contact`() {
    val type = Fake.contactType().apply { wholeOrderLevel = true }
    assertDoesNotThrow { subject.validateAssociatedEntity(type, Fake.requirement(), null, null) }
  }

  @Test
  fun `Successfully validating non-whole order, requirement contact with explicit requirement type`() {
    val requirement = Fake.requirement()
    val type = Fake.contactType().apply {
      wholeOrderLevel = false
      requirementTypeCategories = listOf(requirement.typeCategory!!)
    }
    assertDoesNotThrow { subject.validateAssociatedEntity(type, requirement) }
  }

  @Test
  fun `Attempting to validate invalid requirement contact`() {
    val type = Fake.contactType().apply { wholeOrderLevel = false }
    assertThrows<BadRequestException> { subject.validateAssociatedEntity(type, Fake.requirement()) }
  }

  @Test
  fun `Attempting to validate non-cja 2003 event contact`() {
    val type = Fake.contactType().apply { cjaOrderLevel = false }
    val disposal = Fake.disposal().apply { this.type = Fake.disposalType().apply { cja2003Order = true } }
    assertThrows<BadRequestException> {
      subject.validateAssociatedEntity(type, event = Fake.event().apply { disposals = listOf(disposal) })
    }
  }

  @Test
  fun `Attempting to validate non-legacy event contact`() {
    val type = Fake.contactType().apply { legacyOrderLevel = false }
    val disposal = Fake.disposal().apply { this.type = Fake.disposalType().apply { legacyOrder = true } }
    assertThrows<BadRequestException> {
      subject.validateAssociatedEntity(type, event = Fake.event().apply { disposals = listOf(disposal) })
    }
  }

  @Test
  fun `Successfully validating event contact`() {
    val type = Fake.contactType().apply {
      legacyOrderLevel = true
      cjaOrderLevel = true
    }
    val disposals = listOf(
      Fake.disposal().apply { this.type = Fake.disposalType().apply { cja2003Order = true } },
      Fake.disposal().apply { this.type = Fake.disposalType().apply { legacyOrder = true } }
    )
    assertDoesNotThrow {
      subject.validateAssociatedEntity(type, event = Fake.event().apply { this.disposals = disposals })
    }
  }

  @Test
  fun `Attempting to validate invalid nsi contact`() {
    assertThrows<BadRequestException> { subject.validateAssociatedEntity(Fake.contactType(), nsi = Fake.nsi()) }
  }

  @Test
  fun `Successfully validating nsi contact`() {
    val nsi = Fake.nsi()
    val type = Fake.contactType().apply { nsiTypes = listOf(nsi.type) }
    assertDoesNotThrow { subject.validateAssociatedEntity(type, nsi = nsi) }
  }

  @Test
  fun `Attempting to validate enforcement without outcome`() =
    attemptingToValidateEnforcement(success = false, havingOutcome = false)

  @Test
  fun `Successfully validating null enforcement without outcome`() =
    attemptingToValidateEnforcement(success = true, havingEnforcement = false, havingOutcome = false)

  @Test
  fun `Attempting to validate enforcement with compliant outcome`() =
    attemptingToValidateEnforcement(success = false, compliantAcceptable = true)

  @Test
  fun `Attempting to validate enforcement with non-enforceable outcome`() =
    attemptingToValidateEnforcement(success = false, enforceable = false)

  @Test
  fun `Successfully validating null enforcement with compliant outcome`() =
    attemptingToValidateEnforcement(success = true, havingEnforcement = false, compliantAcceptable = true)

  @Test
  fun `Successfully validating null enforcement with non-enforceable outcome`() =
    attemptingToValidateEnforcement(success = true, havingEnforcement = false, enforceable = false)

  @Test
  fun `Successfully validating enforcement with non-action required outcome`() =
    attemptingToValidateEnforcement(success = true, actionRequired = false)

  @Test
  fun `Attempting to validate null enforcement with action required outcome`() =
    attemptingToValidateEnforcement(success = false, havingEnforcement = false)

  @Test
  fun `Attempting to validate enforcement with not supported enforcement type`() =
    attemptingToValidateEnforcement(success = false, havingEnforcementAction = false)

  @Test
  fun `Successfully validating enforcement`() =
    attemptingToValidateEnforcement(success = true, havingEnforcementAction = true)

  @Test
  fun `Updating outcome meta with non-recorded hours outcome`() {
    val contact = Fake.contact().apply {
      type = Fake.contactType().apply {
        recordedHoursCredited = false
      }
      outcome = Fake.contactOutcomeType().apply {
        attendance = true
        compliantAcceptable = true
      }
    }

    subject.setOutcomeMeta(contact)

    assertThat(contact)
      .hasProperty(Contact::attended, true)
      .hasProperty(Contact::complied, true)
      .hasProperty(Contact::hoursCredited, null)
  }

  @Test
  fun `Updating outcome meta with non-attended recorded hours outcome`() {
    val contact = Fake.contact().apply {
      type = Fake.contactType().apply {
        recordedHoursCredited = true
      }
      outcome = Fake.contactOutcomeType().apply {
        attendance = false
        compliantAcceptable = true
      }
    }

    subject.setOutcomeMeta(contact)

    assertThat(contact)
      .hasProperty(Contact::attended, false)
      .hasProperty(Contact::complied, true)
      .hasProperty(Contact::hoursCredited, null)
  }

  @Test
  fun `Updating outcome meta with recorded hours outcome`() {
    val contact = Fake.contact().apply {
      startTime = LocalTime.of(12, 30)
      endTime = LocalTime.of(14, 0)
      type = Fake.contactType().apply {
        recordedHoursCredited = true
      }
      outcome = Fake.contactOutcomeType().apply {
        attendance = true
        compliantAcceptable = true
      }
    }

    subject.setOutcomeMeta(contact)

    assertThat(contact)
      .hasProperty(Contact::attended, true)
      .hasProperty(Contact::complied, true)
      .hasProperty(Contact::hoursCredited, 1.5)
  }

  private fun attemptingToValidateContactType(
    success: Boolean,
    alert: Boolean = true,
    alertFlag: Boolean = true,
    recordedHoursCredited: Boolean = true,
    havingEndTime: Boolean = true,
  ) {
    val request = Fake.newContact().copy(alert = alert, endTime = if (havingEndTime) LocalTime.NOON else null)
    val type = Fake.contactType().apply {
      this.alertFlag = alertFlag
      this.recordedHoursCredited = recordedHoursCredited
    }

    if (success) {
      assertDoesNotThrow { subject.validateContactType(request, type) }
    } else {
      assertThrows<BadRequestException> { subject.validateContactType(request, type) }
    }
  }

  private fun attemptingToValidateOutcomeType(
    success: Boolean?,
    outcomeFlag: YesNoBoth = YesNoBoth.Y,
    havingRequestOutcome: Boolean = true,
    havingPastDate: Boolean = true,
    havingOutcome: Boolean = true,
    isPermissibleAbsence: Boolean = true,
  ) {
    val request = Fake.newContact().copy(
      outcome = if (havingRequestOutcome) "some-outcome" else null,
      date = if (havingPastDate) Fake.randomPastLocalDate() else Fake.randomFutureLocalDate(),
    )
    val outcomeType = Fake.contactOutcomeType().apply {
      code = if (havingOutcome) "some-outcome" else "some-other-outcome"
      attendance = !isPermissibleAbsence
      compliantAcceptable = isPermissibleAbsence
    }
    val type = Fake.contactType().apply {
      this.outcomeFlag = outcomeFlag
      outcomeTypes = listOf(outcomeType)
    }

    if (success == false) {
      assertThrows<BadRequestException> { subject.validateOutcomeType(request, type) }
    } else {
      val observed = subject.validateOutcomeType(request, type)
      if (success == null) {
        assertThat(observed).isEqualTo(null)
      } else {
        assertThat(observed).isSameAs(outcomeType)
      }
    }
  }

  private fun attemptingToValidateOfficeLocation(
    success: Boolean?,
    locationFlag: YesNoBoth = YesNoBoth.Y,
    havingRequestOfficeLocation: Boolean = true,
    havingOfficeLocation: Boolean = true,
  ) {
    val request = Fake.newContact().copy(
      officeLocation = if (havingRequestOfficeLocation) "some-office-location" else null
    )
    val type = Fake.contactType().apply { this.locationFlag = locationFlag }
    val officeLocation = Fake.officeLocation().apply {
      code = if (havingOfficeLocation) "some-office-location" else "some-other-office-location"
    }
    val team = Fake.team().apply {
      officeLocations = listOf(officeLocation)
    }

    if (success == false) {
      assertThrows<BadRequestException> { subject.validateOfficeLocation(request, type, team) }
    } else {
      val observed = subject.validateOfficeLocation(request, type, team)
      if (success == null) {
        assertThat(observed).isEqualTo(null)
      } else {
        assertThat(observed).isSameAs(officeLocation)
      }
    }
  }

  private fun attemptingToValidateFutureAppointmentClashes(
    success: Boolean,
    attendanceContact: Boolean = true,
    havingEndTime: Boolean = true,
    havingFutureDate: Boolean = true,
    havingClashes: Boolean? = null,
  ) {
    val request = Fake.newContact().copy(
      endTime = if (havingEndTime) LocalTime.NOON else null,
      date = if (havingFutureDate) Fake.randomFutureLocalDate() else Fake.randomPastLocalDate()
    )
    val type = Fake.contactType().apply { this.attendanceContact = attendanceContact }
    val offender = Fake.offender()
    val existing = Fake.contact()

    if (havingClashes != null) {
      whenever(
        contactRepository.findClashingAttendanceContacts(
          offender.id,
          request.date,
          request.startTime,
          request.endTime as LocalTime,
        )
      ).thenReturn(if (havingClashes) listOf(existing, Fake.contact()) else listOf(existing))
    }

    if (success) {
      assertDoesNotThrow { subject.validateFutureAppointmentClashes(request, type, offender, existing.id) }
    } else {
      assertThrows<ConflictException> { subject.validateFutureAppointmentClashes(request, type, offender, existing.id) }
    }
  }

  private fun attemptingToValidateEnforcement(
    success: Boolean,
    havingEnforcement: Boolean = true,
    havingOutcome: Boolean = true,
    compliantAcceptable: Boolean = false,
    enforceable: Boolean = true,
    actionRequired: Boolean = true,
    havingEnforcementAction: Boolean? = null,
  ) {
    val request = Fake.newContact().copy(
      enforcement = if (havingEnforcement) "some-enforcement" else null
    )
    val action = Fake.enforcementAction().apply {
      code = "some-enforcement"
      responseByPeriod = 7
    }

    if (havingEnforcementAction != null) {
      whenever(enforcementActionRepository.findByCode("some-enforcement"))
        .thenReturn(if (havingEnforcementAction) action else null)
    }

    val outcome = if (havingOutcome) Fake.contactOutcomeType().apply {
      this.compliantAcceptable = compliantAcceptable
      this.enforceable = enforceable
      this.actionRequired = actionRequired
    } else null

    if (success) {
      val observed = subject.validateEnforcement(request, outcome)
      if (havingEnforcement) {
        assertThat(observed)
          .usingRecursiveComparison()
          .comparingDateTimesToNearestSecond()
          .isEqualTo(
            Enforcement(
              actionTakenDate = LocalDate.now(),
              actionTakenTime = LocalTime.now(),
              action = if (actionRequired) action else null,
              responseDate = if (actionRequired) LocalDate.now().plusDays(7) else null
            )
          )
      } else {
        assertThat(observed).isEqualTo(null)
      }
    } else {
      assertThrows<BadRequestException> { subject.validateEnforcement(request, outcome) }
    }
  }
}
