package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object Fake {
  val faker = Faker()
  val mapper = ContactMapper.INSTANCE

  fun localDateTime(): LocalDateTime = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
  fun localDate(): LocalDate = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  fun localTime(): LocalTime = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

  inline fun <reified Partial : Any> newContact(partial: Partial?) = NewContact(
    offenderId = faker.number().randomNumber(),
    contactType = faker.lorem().characters(1, 10),
    contactOutcome = faker.lorem().characters(1, 10),
    provider = faker.lorem().characters(3),
    team = faker.lorem().characters(6),
    staff = faker.lorem().characters(7),
    officeLocation = faker.lorem().characters(7),
    contactDate = localDate(),
    contactStartTime = localTime(),
    contactEndTime = localTime(),
    alert = faker.bool().bool(),
    sensitive = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    contactShortDescription = faker.company().bs(),
  ).merge(partial)

  fun newContact() = newContact(null)

  inline fun <reified Partial : Any> contactDto(partial: Partial?) = mapper.toDto(newContact())
    .merge(object { val id = faker.number().randomNumber() })
    .merge(partial)

  fun contactDto() = contactDto(null)

  inline fun <reified Partial : Any> contact(partial: Partial?) = Contact(
    id = faker.number().randomNumber(),
    offender = Offender(id = faker.number().randomNumber()),
    contactType = ContactType(id = faker.number().randomNumber(), code = faker.lorem().characters(1, 10)),
    contactOutcomeType = ContactOutcomeType(id = faker.number().randomNumber(), code = faker.lorem().characters(1, 10)),
    provider = Provider(id = faker.number().randomNumber(), code = faker.lorem().characters(3)),
    team = Team(id = faker.number().randomNumber(), code = faker.lorem().characters(6)),
    staff = Staff(id = faker.number().randomNumber(), code = faker.lorem().characters(7)),
    officeLocation = OfficeLocation(id = faker.number().randomNumber(), code = faker.lorem().characters(7)),
    contactDate = localDate(),
    contactStartTime = localTime(),
    contactEndTime = localTime(),
    alert = faker.bool().bool(),
    sensitive = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    createdByUserId = faker.number().randomNumber(),
    lastUpdatedUserId = faker.number().randomNumber(),
    partitionAreaId = faker.number().randomNumber(),
    staffEmployeeId = faker.number().randomNumber(),
    teamProviderId = faker.number().randomNumber(),
    createdDateTime = localDateTime(),
    lastUpdatedDateTime = localDateTime(),
  ).merge(partial)

  fun contact() = contact(null)

  /**
   * Merge all properties of partial into target.
   */
  inline fun <reified T : Any, reified Partial : Any> T.merge(partial: Partial?): T {
    if (partial == null) {
      return this
    }

    val props = T::class.declaredMemberProperties.associateBy { it.name }
    val partialProps = Partial::class.declaredMemberProperties.associateBy { it.name }

    val primaryConstructor = T::class.primaryConstructor
      ?: throw IllegalArgumentException("merge type must have a primary constructor")

    val args = primaryConstructor.parameters.associateWith { parameter ->
      when {
        partialProps.containsKey(parameter.name) -> partialProps[parameter.name]?.get(partial)
        props.containsKey(parameter.name) -> props[parameter.name]?.get(this)
        else -> throw IllegalStateException("no declared member property found with name '${parameter.name}'")
      }
    }

    return primaryConstructor.callBy(args)
  }
}
