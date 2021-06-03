package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class ContactMapperTest {

  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    assertThat(observed)
      .hasProperty(ContactDto::id, source.id)
      .hasProperty(ContactDto::offenderCrn, source.offender.crn)
      .hasProperty(ContactDto::type, source.type.code)
      .hasProperty(ContactDto::typeDescription, source.type.description)
      .hasProperty(ContactDto::outcome, source.outcome?.code)
      .hasProperty(ContactDto::outcomeDescription, source.outcome?.description)
      .hasProperty(ContactDto::enforcement, source.enforcements[0].action?.code!!)
      .hasProperty(ContactDto::enforcementDescription, source.enforcements[0].action?.description!!)
      .hasProperty(ContactDto::provider, source.provider?.code)
      .hasProperty(ContactDto::providerDescription, source.provider?.description)
      .hasProperty(ContactDto::team, source.team?.code)
      .hasProperty(ContactDto::teamDescription, source.team?.description)
      .hasProperty(ContactDto::staff, source.staff?.code)
      .hasProperty(ContactDto::staffFirstName, source.staff?.firstName)
      .hasProperty(ContactDto::staffLastName, source.staff?.lastName)
      .hasProperty(ContactDto::officeLocation, source.officeLocation?.code)
      .hasProperty(ContactDto::officeLocationDescription, source.officeLocation?.description)
      .hasProperty(ContactDto::date, source.date)
      .hasProperty(ContactDto::startTime, source.startTime)
      .hasProperty(ContactDto::endTime, source.endTime)
      .hasProperty(ContactDto::alert, source.alert)
      .hasProperty(ContactDto::sensitive, source.sensitive)
      .hasProperty(ContactDto::notes, source.notes)
      .hasProperty(ContactDto::description, source.description)
      .hasProperty(ContactDto::eventId, source.event?.id!!)
      .hasProperty(ContactDto::requirementId, source.requirement?.id!!)
      .hasProperty(ContactDto::rarActivity, source.rarActivity!!)
  }

  @Test
  fun `Mapping from entity to update`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toUpdate(source)

    assertThat(observed)
      .hasProperty(UpdateContact::outcome, source.outcome?.code)
      .hasProperty(UpdateContact::enforcement, source.enforcements[0].action?.code!!)
      .hasProperty(UpdateContact::provider, source.provider?.code)
      .hasProperty(UpdateContact::team, source.team?.code)
      .hasProperty(UpdateContact::staff, source.staff?.code)
      .hasProperty(UpdateContact::officeLocation, source.officeLocation?.code)
      .hasProperty(UpdateContact::date, source.date)
      .hasProperty(UpdateContact::startTime, source.startTime)
      .hasProperty(UpdateContact::endTime, source.endTime)
      .hasProperty(UpdateContact::alert, source.alert)
      .hasProperty(UpdateContact::sensitive, source.sensitive)
      .hasProperty(UpdateContact::notes, null)
      .hasProperty(UpdateContact::description, source.description)
      .hasProperty(UpdateContact::rarActivity, source.rarActivity!!)
  }
}
