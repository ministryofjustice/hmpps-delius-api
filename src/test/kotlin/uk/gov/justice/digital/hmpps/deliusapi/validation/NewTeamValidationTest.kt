package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam

class NewTeamValidationTest : ValidationTest<NewTeam>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid new team`(case: ValidationTestCase<NewTeam>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid new team`(case: ValidationTestCase<NewTeam>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<NewTeam>()
      .setValid()
      .kitchenSink()
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<NewTeam>()
      .string(NewTeam::code) { it.empty().blank().length(7) }
      .string(NewTeam::description) { it.empty().blank().length(51) }
      .string(NewTeam::cluster) { it.empty().blank() }
      .string(NewTeam::ldu) { it.empty().blank() }
      .string(NewTeam::provider) { it.empty().blank() }
      .string(NewTeam::type) { it.empty().blank() }
      .cases
  }
}
