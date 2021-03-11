package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.CreateOrUpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isPermissibleAbsence
import java.time.LocalDate
import java.time.LocalTime

@Service
class ContactValidationService(private val contactRepository: ContactRepository) {
  fun validateContactType(request: CreateOrUpdateContact, type: ContactType) {
    if (request.alert && !type.alertFlag) {
      throw BadRequestException("Contact type '${type.code}' does not support alert")
    }

    if (type.recordedHoursCredited && request.endTime == null) {
      throw BadRequestException("Contact type '${type.code}' requires an end time")
    }
  }

  fun validateOutcomeType(request: CreateOrUpdateContact, type: ContactType): ContactOutcomeType? {
    if (type.outcomeFlag == YesNoBoth.Y && request.outcome == null && request.date.isBefore(LocalDate.now())) {
      throw BadRequestException("Contact type '${type.code}' requires an outcome type")
    }

    val outcome = if (request.outcome != null)
      type.outcomeTypes?.find { it.code == request.outcome }
        ?: throw BadRequestException("Contact type with code '${type.code}' does not support outcome code '${request.outcome}'")
    else null

    if (outcome?.isPermissibleAbsence() == false && request.date.isAfter(LocalDate.now())) {
      throw BadRequestException("Outcome code '${request.outcome}' not a permissible absence - only permissible absences can be recorded for a future attendance")
    }

    return outcome
  }

  fun validateOfficeLocation(request: CreateOrUpdateContact, type: ContactType, team: Team): OfficeLocation? {
    fun get() = team.officeLocations?.find { it.code == request.officeLocation }

    return when (type.locationFlag) {
      YesNoBoth.Y -> {
        if (request.officeLocation == null) {
          throw BadRequestException("Location is required for contact type '${type.code}'")
        }
        get() ?: throw BadRequestException("Team with code '${request.team}' does not exist at office location '${request.officeLocation}'")
      }
      YesNoBoth.N -> {
        if (request.officeLocation != null) {
          throw BadRequestException("Contact type '${type.code}' does not support a location")
        }
        null
      }
      YesNoBoth.B -> get()
    }
  }

  fun validateFutureAppointmentClashes(request: CreateOrUpdateContact, type: ContactType, offender: Offender, existingId: Long? = null) {
    if (!type.attendanceContact || request.endTime == null || !request.date.isAfter(LocalDate.now())) {
      return
    }

    val clashes = contactRepository.findClashingAttendanceContacts(
      offender.id,
      request.date,
      request.startTime,
      request.endTime as LocalTime,
    ).filter { it.id != existingId }

    if (clashes.isNotEmpty()) {
      val ids = clashes.joinToString(", ") { "'${it.id}'" }
      throw BadRequestException(
        "Contact type '${type.code}' is an attendance type so must not clash with any other " +
          "attendance contacts but clashes with contacts with ids $ids"
      )
    }
  }

  fun validateAssociatedEntity(
    type: ContactType,
    requirement: Requirement? = null,
    event: Event? = null,
    nsi: Nsi? = null,
  ) {
    if (nsi != null) {
      // Contact is at nsi level - contact type must support nsi type
      if (type.nsiTypes?.any { it.id == nsi.type?.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for an NSI with type '${nsi.type?.code}'")
      }
    } else if (requirement != null) {
      // Contact is at "Whole Order" level - type must be whole order level OR have matching requirement type category.
      if (!type.wholeOrderLevel && type.requirementTypeCategories?.any { it.id == requirement.typeCategory?.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for an requirement in category '${requirement.typeCategory?.code}'")
      }
    } else if (event != null) {
      // Contact is at event level - type must support relevant pre/post CJA 2003 status of event.
      // It looks like in Delius that these are not mutually exclusive!
      val isLegacy = event.disposals?.any { it.type?.legacyOrder == true } ?: false
      if (isLegacy && !type.legacyOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a pre CJA 2003 event")
      }
      val isCja = event.disposals?.any { it.type?.cja2003Order == true } ?: false
      if (isCja && !type.cjaOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a CJA 2003 event")
      }
    } else {
      // Contact is at offender level
      if (!type.offenderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate at offender level")
      }
    }
  }
}
