package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff

class NewStaffValidationTest : ValidationTest<NewStaff>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid new staff`(case: ValidationTestCase<NewStaff>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid new staff`(case: ValidationTestCase<NewStaff>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<NewStaff>()
      .setValid()
      .kitchenSink()
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<NewStaff>()
      .string(NewStaff::firstName) { it.empty().blank().length(36) }
      .string(NewStaff::lastName) { it.empty().blank().length(36) }
      .string(NewStaff::provider) { it.empty().blank().value("bacon", "not a valid provider code") }
      .list(NewStaff::teams) { it.empty() }
      .cases
  }
}
