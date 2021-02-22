package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsiManager

class NewNsiManagerValidationTest : ValidationTest<NewNsiManager>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid new nsi manager`(case: ValidationTestCase<NewNsiManager>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid new nsi manager`(case: ValidationTestCase<NewNsiManager>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<NewNsiManager>()
      .setValid()
      .kitchenSink()
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<NewNsiManager>()
      .string(NewNsiManager::staff) { it.empty().blank().length(6).length(8) }
      .string(NewNsiManager::team) { it.empty().blank().length(5).length(7) }
      .string(NewNsiManager::provider) { it.empty().blank().length(2).length(4) }
      .cases
  }
}
