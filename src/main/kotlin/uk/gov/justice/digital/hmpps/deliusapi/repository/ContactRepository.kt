package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.RequirementTypeCategory.Companion.RAR_REQUIREMENT_TYPE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.deliusapi.repository.models.LocalDateTimeWrapper
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface ContactRepository : JpaRepository<Contact, Long> {
  fun findAllByNsiId(nsiId: Long): List<Contact>

  @Query(
    "select c from Contact c where c.offender.id = :offenderId and c.type.attendanceContact = true " +
      "and c.date = :date and c.startTime < :endTime and c.endTime > :startTime"
  )
  fun findClashingAttendanceContacts(
    @Param("offenderId") offenderId: Long,
    @Param("date") date: LocalDate,
    @Param("startTime") startTime: LocalTime,
    @Param("endTime") endTime: LocalTime,
  ): List<Contact>

  fun findAllByTypeId(typeId: Long): List<Contact>

  @Query(
    "select count(distinct c.date) from Contact c where c.complied = false and c.event.id = :eventId " +
      "and c.type.nationalStandardsContact = true and (:lastResetDate is null or c.date >= :lastResetDate)"
  )
  fun countFailureToComply(@Param("eventId") eventId: Long, @Param("lastResetDate") lastResetDate: LocalDate?): Long

  @Query(
    "select count(c.id) from Contact c where c.event.id = :eventId and c.type.code = :contactCode " +
      "and c.outcome is null and (:breachEnd is null or c.date >= :breachEnd)"
  )
  fun countEnforcementUnderReview(
    @Param("eventId") eventId: Long,
    @Param("contactCode") contactCode: String,
    @Param("breachEnd") breachEnd: LocalDate?
  ): Long

  @Query(
    "select new uk.gov.justice.digital.hmpps.deliusapi.repository.models.LocalDateTimeWrapper(c.date, c.startTime) from Contact c where c.event.id = :eventId " +
      "and c.type.code in :breachContactTypes order by c.date DESC, c.startTime DESC"
  )
  fun findAllBreachDates(
    @Param("eventId") eventId: Long,
    @Param("breachContactTypes") breachContactTypes: List<String>
  ): List<LocalDateTimeWrapper>

  fun findAllByNsiIdAndTypeIdAndDate(nsiId: Long, typeId: Long, date: LocalDate): List<Contact>

  fun findAllByNsiIdAndTypeCode(nsiId: Long, typeCode: String): List<Contact>

  fun deleteAllByEventIdAndTypeNationalStandardsContactIsTrue(eventId: Long)

  fun deleteAllByEventIdAndTypeCode(eventId: Long, typeCode: String)

  fun findAllByEventIdAndTypeCode(eventId: Long, typeCode: String): List<Contact>

  @Query(
    "select count(distinct c.date) from Contact c where c.rarActivity = true " +
      "and c.requirement.id = :requirementId " +
      "and c.softDeleted = false AND c.requirement.typeCategory.code = '$RAR_REQUIREMENT_TYPE_CATEGORY_CODE'"
  )
  fun countRequirementRar(@Param("requirementId") requirementId: Long): Long

  @Query(
    "select count(distinct c.date) from Contact c where c.rarActivity = true " +
      "and c.nsi.id = :nsiId " +
      "and c.softDeleted = false AND c.nsi.requirement.typeCategory.code = '$RAR_REQUIREMENT_TYPE_CATEGORY_CODE'"
  )
  fun countNsiRar(@Param("nsiId") nsiId: Long): Long

  fun deleteAllByRequirementIdAndDate(requirementId: Long, date: LocalDate)

  fun deleteAllByNsiIdAndDate(nsiId: Long, date: LocalDate)

  fun deleteAllByOffenderCrnAndDateAndStartTimeAndEndTime(offenderCrn: String, date: LocalDate, startTime: LocalTime, endTime: LocalTime)
}

fun ContactRepository.isEnforcementUnderReview(eventId: Long, contactCode: String, breachEnd: LocalDate?) =
  countEnforcementUnderReview(eventId, contactCode, breachEnd) > 0
