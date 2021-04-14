package uk.gov.justice.digital.hmpps.deliusapi.config

import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewTeam
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

typealias ContactSelector = KProperty1<ContactTestsConfiguration, ContactTestConfiguration>

fun EndToEndTestConfiguration.newContact(select: ContactSelector): NewContact {
  val contact = select(contacts)
  return NewContact(
    offenderCrn = offenderCrn,
    type = contact.type,
    outcome = contact.outcome,
    enforcement = contact.enforcement,
    provider = provider,
    team = team,
    staff = staff,
    officeLocation = contact.officeLocation,
    eventId = contact.eventId,
    requirementId = contact.requirementId,
    nsiId = null,
    date = LocalDate.now(),
    startTime = "12:00",
    endTime = "13:00",
    sensitive = false,
    alert = null,
    notes = "Test contact from Delius API",
    description = "Test contact from Delius API",
  )
}

typealias NsiSelector = KProperty1<NsiTestsConfiguration, NsiTestConfiguration>

fun EndToEndTestConfiguration.newNsi(select: NsiSelector): NewNsi {
  val nsi = select(nsis)
  return NewNsi(
    offenderCrn = offenderCrn,
    type = nsi.type,
    subType = nsi.subType,
    outcome = nsi.outcome,
    intendedProvider = provider,
    manager = NewNsiManager(
      provider = provider,
      team = team,
      staff = staff,
    ),
    eventId = nsi.eventId,
    requirementId = nsi.requirementId,
    notes = "Delius API e2e tests",
    status = nsi.status,
    length = nsi.length,
    statusDate = LocalDateTime.now().minusHours(1).withNano(0),
    referralDate = LocalDate.now(),
    startDate = LocalDate.now(),
    expectedStartDate = LocalDate.now(),
    expectedEndDate = LocalDate.now().plusDays(7),
  )
}

typealias StaffSelector = KProperty1<StaffTestsConfiguration, StaffTestConfiguration>

fun EndToEndTestConfiguration.newStaff(select: StaffSelector): NewStaff {
  val staffMember = select(staffs)
  return NewStaff(
    firstName = staffMember.firstName,
    lastName = staffMember.lastName,
    provider = staffMember.provider,
    teams = staffMember.teams
  )
}

typealias TeamSelector = KProperty1<TeamTestsConfiguration, TeamTestConfiguration>

fun EndToEndTestConfiguration.newTeam(select: TeamSelector): NewTeam {
  val team = select(teams)
  return NewTeam(
    cluster = team.cluster,
    description = team.description,
    ldu = team.ldu,
    provider = team.provider,
    type = team.type,
    unpaidWorkTeam = team.unpaidWorkTeam,
  )
}
