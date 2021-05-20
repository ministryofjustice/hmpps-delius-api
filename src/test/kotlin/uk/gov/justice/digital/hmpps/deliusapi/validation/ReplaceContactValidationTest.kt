package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ReplaceContact

class ReplaceContactValidationTest : ValidationTest<ReplaceContact>() {

  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid replace contact`(case: ValidationTestCase<ReplaceContact>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid replace contact`(case: ValidationTestCase<ReplaceContact>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<ReplaceContact>()
      .setValid()
      .kitchenSink()
      .string(ReplaceContact::outcome) { it.length(1).length(10) }
      .time(ReplaceContact::endTime) { it.isNull() }
      .number(ReplaceContact::requirementId) { it.isNull() }
      .allNull(ReplaceContact::requirementId, ReplaceContact::eventId)
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<ReplaceContact>()
      .string(ReplaceContact::offenderCrn) { it.empty().blank().value("bacon", "not a valid crn") }
      .string(ReplaceContact::outcome) { it.empty().blank().length(11) }
      .time(ReplaceContact::endTime) { it.before(ReplaceContact::startTime) }
      .number(ReplaceContact::eventId) { it.zero().negative() }
      .number(ReplaceContact::requirementId) { it.zero().negative().dependent(ReplaceContact::eventId) }
      .number(ReplaceContact::nsiId) { it.zero().negative().exclusive(34563, 12345, ReplaceContact::requirementId) }
      .cases
  }
}
