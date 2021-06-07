package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.CreateOrUpdateNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

@Service
class NsiValidationService {
  fun assertTypeConstraints(type: NsiType, request: CreateOrUpdateNsi) {
    val active = request.endDate == null
    if (active && !type.allowActiveDuplicates || !active && !type.allowInactiveDuplicates) {
      throw BadRequestException(
        "NSI type '${type.code}' does not allow ${if (active) "active" else "inactive"} duplicates & duplicate checking is not yet implemented"
      )
    }

    val length = request.length
    if (type.units == null) {
      if (length != null) {
        throw BadRequestException("NSI type '${type.code}' does not support a length")
      }
    } else {
      if (length == null) {
        throw BadRequestException("NSI type '${type.code}' requires a length in units ${type.units?.code}")
      }

      if (length < type.minimumLength ?: Long.MIN_VALUE || length > type.maximumLength ?: Long.MAX_VALUE) {
        throw BadRequestException("NSI type '${type.code}' requires a length between ${type.minimumLength} & ${type.maximumLength} ${type.units?.code}")
      }
    }
  }

  fun validateStatus(type: NsiType, request: CreateOrUpdateNsi): NsiStatus {
    return type.statuses?.find { it.code == request.status }
      ?: throw BadRequestException("NSI status '${request.status}' is not a valid status of NSI type '${type.code}'")
  }

  fun validateOutcome(type: NsiType, request: CreateOrUpdateNsi): StandardReference? {
    return if (request.outcome == null) null
    else type.outcomes?.find { it.code == request.outcome }
      ?: throw BadRequestException("Outcome type '${request.outcome}' is not a valid outcome type of '${type.code}'")
  }

  fun assertRequirementConstraints(type: NsiType, requirement: Requirement, request: CreateOrUpdateNsi) {
    if (requirement.typeCategory?.nsiTypes?.contains(type) != true) {
      throw BadRequestException("Requirement '${requirement.id}' is not in a category that supports NSIs of type ${type.code}")
    }

    if (requirement.terminationDate != null) {
      if (request.endDate == null) {
        throw BadRequestException("End date is required as requirement has termination date '${requirement.terminationDate}'")
      }

      if (request.endDate!! < requirement.terminationDate) {
        throw BadRequestException("End date must not be before the requirement termination date '${requirement.terminationDate}'")
      }
    }
  }

  fun assertSupportedTypeLevel(nsi: Nsi) {
    val level = if (nsi.event == null) NsiTypeLevel.OFFENDER else NsiTypeLevel.EVENT
    val supported = getSupportedTypeLevels(nsi.type)
    if (supported.contains(level)) {
      return
    }

    val names = supported.joinToString(" or ") { it.name.lowercase() }
    throw BadRequestException(
      "NSI type with code '${nsi.type.code}' supports association to $names only " +
        "& this request is attempting to associate to ${level.name.lowercase()}"
    )
  }

  private enum class NsiTypeLevel { OFFENDER, EVENT }

  private fun getSupportedTypeLevels(type: NsiType): Set<NsiTypeLevel> {
    val levels = mutableSetOf<NsiTypeLevel>()
    if (type.offenderLevel) {
      levels.add(NsiTypeLevel.OFFENDER)
    }
    if (type.eventLevel) {
      levels.add(NsiTypeLevel.EVENT)
    }
    return levels.toSet()
  }
}
