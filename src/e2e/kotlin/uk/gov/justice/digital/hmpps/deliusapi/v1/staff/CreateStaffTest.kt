
package uk.gov.justice.digital.hmpps.deliusapi.v1.staff

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.test.context.junit.jupiter.DisabledIf
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.client.model.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.StaffTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newStaff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StaffTeam
import uk.gov.justice.digital.hmpps.deliusapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate

class CreateStaffTest @Autowired constructor(
  private val repository: StaffRepository,
) :
  EndToEndTest() {
  private lateinit var request: NewStaff
  private lateinit var response: StaffDto
  private lateinit var created: Staff

  @Test
  @Transactional
  @Commit
  @DisabledIf(
    expression = "#{'\${spring.profiles.active}' == 'local-h2'}",
    reason = "H2 doesn't have the getNextStaffReference function"
  )
  fun `Creating staff with team`() {
    request = configuration.newStaff(StaffTestsConfiguration::withTeam)
    whenCreatingStaff()
    shouldReturnExpectedStaff()
    shouldSaveStaff()
    removeInsertedRows()
  }

  private fun whenCreatingStaff() {
    response = staffV1.safely { it.createStaff(request) }
  }

  private fun shouldReturnExpectedStaff() {
    assertThat(response)
      .hasFieldOrProperty("code")
      .hasFieldOrProperty("privateStaff")
      .hasProperty(StaffDto::firstName, request.firstName)
      .hasProperty(StaffDto::lastName, request.lastName)
      .hasProperty(StaffDto::provider, request.provider)
      .hasProperty(StaffDto::teams, request.teams)

    assertThat(response.code).startsWith(request.provider)
    assertThat(response.startDate).isEqualTo(LocalDate.now())
  }

  private fun shouldSaveStaff() {
    if (!databaseAssertEnabled()) {
      return
    }

    created = repository.findByCode(response.code)
      ?: throw RuntimeException("Staff with code = '${response.code}' does not exist in the database")

    assertThat(created)
      .hasFieldOrPropertyWithValue("code", response.code)
      .hasProperty(Staff::firstName, request.firstName)
      .hasProperty(Staff::lastName, request.lastName)
      .hasProperty(Staff::privateStaff, created.provider!!.privateTrust)
      .extracting("teams")
      .asList().haveExactly(
        1,
        Condition(
          { m: StaffTeam -> m.team.code == request.teams!!.first() } as ((Any) -> Boolean)?,
          "team"
        )
      )

    assertThat(created.provider)
      .hasFieldOrPropertyWithValue("code", request.provider)
  }

  private fun removeInsertedRows() {
    if (databaseAssertEnabled()) {
      repository.delete(created)
    }
  }
}
