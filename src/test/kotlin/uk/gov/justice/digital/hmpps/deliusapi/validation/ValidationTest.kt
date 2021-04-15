package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.validation.Validator
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

data class ValidationTestCase<T : Any>(
  val name: String,
  val subject: T,
  val invalidPaths: List<String>,
  val strict: Boolean = true
)

data class RawValidationTestCase(
  val name: String,
  val args: Map<String, Any?>,
  val invalidPaths: List<String>,
)

abstract class PropertyCaseBuilder<T : Any, P, Me : PropertyCaseBuilder<T, P, Me>>(
  property: KProperty1<T, P>,
  private val builder: ValidationTestCaseBuilder<T>
) {

  protected val name = property.name
  val subject = builder.factory(null, null)
  val cases = mutableListOf<RawValidationTestCase>()

  fun value(value: P, reason: String): Me =
    add("$reason $name = '$value'", mapOf(name to value), name)

  fun isNull(): Me = add("$name is null", mapOf(name to null), name)

  fun dependent(other: KProperty1<T, *>) =
    add("$name is dependent on ${other.name}", mapOf(other.name to null), name)

  fun exclusive(value: P, otherValue: P, other: KProperty1<T, *>) =
    add("$name can't be present with ${other.name}", mapOf(name to value, other.name to otherValue), name)

  protected fun add(name: String, args: Map<String, Any?>, vararg invalidPaths: String): Me {
    cases.add(RawValidationTestCase(name, args, if (builder.valid) listOf() else invalidPaths.toList()))
    @Suppress("UNCHECKED_CAST") return this as Me
  }
}

class StringPropertyCaseBuilder<T : Any>(property: KProperty1<T, String?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, String?, StringPropertyCaseBuilder<T>>(property, builder) {

  fun empty() = value("", "empty")
  fun blank() = value(" ", "blank")
  fun length(n: Int) = value(Fake.faker.lorem().characters(n), "with length $n")
}

class LongPropertyCaseBuilder<T : Any>(property: KProperty1<T, Long?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, Long?, LongPropertyCaseBuilder<T>>(property, builder) {

  fun zero() = value(0L, "zero")
  fun negative() = value(-1L, "negative")
}

class LocalDatePropertyCaseBuilder<T : Any>(property: KProperty1<T, LocalDate?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, LocalDate?, LocalDatePropertyCaseBuilder<T>>(property, builder) {

  fun before(other: KProperty1<T, LocalDate?>) =
    add("$name is before ${other.name}", mapOf(name to other.get(subject)?.minusDays(1)), name, other.name)

  fun tomorrow() = value(LocalDate.now().plusDays(1), "tomorrow")
}

class LocalDateTimePropertyCaseBuilder<T : Any>(property: KProperty1<T, LocalDateTime?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, LocalDateTime?, LocalDateTimePropertyCaseBuilder<T>>(property, builder) {

  fun beforeDate(other: KProperty1<T, LocalDate?>) =
    add(
      "$name is before ${other.name}",
      mapOf(name to LocalDateTime.of(other.get(subject)?.minusDays(1), LocalTime.MIDNIGHT)),
      name,
      other.name
    )

  fun tomorrow() = value(LocalDateTime.now().plusDays(1), "tomorrow")
}

class LocalTimePropertyCaseBuilder<T : Any>(property: KProperty1<T, LocalTime?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, LocalTime?, LocalTimePropertyCaseBuilder<T>>(property, builder) {

  fun before(other: KProperty1<T, LocalTime?>) =
    add("$name is before ${other.name}", mapOf(name to other.get(subject)?.minusSeconds(1)), name, other.name)
}

class ListPropertyCaseBuilder<T : Any>(property: KProperty1<T, List<*>?>, builder: ValidationTestCaseBuilder<T>) :
  PropertyCaseBuilder<T, List<*>?, ListPropertyCaseBuilder<T>>(property, builder) {

  fun empty() = value(listOf<T>(), "empty")
}

class ValidationTestCaseBuilder<T : Any>(val factory: (existing: T?, parameters: Map<String, Any?>?) -> T) {

  var valid = false
  val cases = mutableListOf<ValidationTestCase<T>>()

  fun setValid(): ValidationTestCaseBuilder<T> {
    valid = true
    return this
  }

  fun string(property: KProperty1<T, String?>, delegate: (b: StringPropertyCaseBuilder<T>) -> StringPropertyCaseBuilder<T>) =
    add(StringPropertyCaseBuilder(property, this), delegate)

  fun number(property: KProperty1<T, Long?>, delegate: (b: LongPropertyCaseBuilder<T>) -> LongPropertyCaseBuilder<T>) =
    add(LongPropertyCaseBuilder(property, this), delegate)

  fun date(property: KProperty1<T, LocalDate?>, strict: Boolean = true, delegate: (b: LocalDatePropertyCaseBuilder<T>) -> LocalDatePropertyCaseBuilder<T>) =
    add(LocalDatePropertyCaseBuilder(property, this), delegate, strict)

  fun dateTime(property: KProperty1<T, LocalDateTime?>, strict: Boolean = true, delegate: (b: LocalDateTimePropertyCaseBuilder<T>) -> LocalDateTimePropertyCaseBuilder<T>) =
    add(LocalDateTimePropertyCaseBuilder(property, this), delegate, strict)

  fun time(property: KProperty1<T, LocalTime?>, strict: Boolean = true, delegate: (b: LocalTimePropertyCaseBuilder<T>) -> LocalTimePropertyCaseBuilder<T>) =
    add(LocalTimePropertyCaseBuilder(property, this), delegate, strict)

  fun list(property: KProperty1<T, List<*>?>, delegate: (b: ListPropertyCaseBuilder<T>) -> ListPropertyCaseBuilder<T>) =
    add(ListPropertyCaseBuilder(property, this), delegate)

  fun kitchenSink() = add("kitchen sink", mapOf())

  fun <P> allNull(vararg properties: KProperty1<T, P?>) =
    add(properties.joinToString(" & ") { it.name } + " are null", properties.associateBy({ it.name }, { null }))

  fun add(name: String, vararg invalidPaths: String, strict: Boolean = false, fn: (subject: T) -> T): ValidationTestCaseBuilder<T> {
    val subject = fn(factory(null, null))
    cases.add(ValidationTestCase(name, subject, invalidPaths.toList(), strict))
    return this
  }

  private fun add(name: String, args: Map<String, Any?>, vararg invalidPaths: String, strict: Boolean = true) =
    add(RawValidationTestCase(name, args, invalidPaths.toList()), strict = strict)

  private fun <B : PropertyCaseBuilder<T, *, *>> add(builder: B, delegate: (b: B) -> B, strict: Boolean = true): ValidationTestCaseBuilder<T> {
    for (case in delegate(builder).cases) {
      add(case, builder.subject, strict)
    }
    return this
  }

  private fun add(case: RawValidationTestCase, subject: T? = null, strict: Boolean = false): ValidationTestCaseBuilder<T> {
    cases.add(ValidationTestCase(case.name, factory(subject, case.args), case.invalidPaths, strict))
    return this
  }

  companion object {
    /**
     * Get a validation test case builder with a suitable factory chosen from the static Fake object.
     */
    inline fun <reified T : Any> fromFake(): ValidationTestCaseBuilder<T> {
      val type = T::class
      val factory = Fake::class.functions.find {
        it.javaMethod?.returnType == type.java && it.parameters.none { p -> p.kind != KParameter.Kind.INSTANCE }
      } ?: throw RuntimeException("No faker factory found for type ${type.simpleName}")

      return fromFactory { factory.call(Fake) as T }
    }

    inline fun <reified T : Any> fromFactory(crossinline subjectFactory: () -> T): ValidationTestCaseBuilder<T> {
      val copy = T::class.memberFunctions.find { it.name == "copy" }
        ?: throw RuntimeException("No copy method on type, is this a data class?")

      return ValidationTestCaseBuilder { existing, parameters ->
        val subject = existing ?: subjectFactory()
        if (parameters == null) subject else {
          val map = parameters.mapKeys { (k) -> copy.parameters.find { it.name == k } ?: throw RuntimeException("No property $k") }
            .plus(copy.instanceParameter!! to subject)
          copy.callBy(map) as T
        }
      }
    }
  }
}

@ExtendWith(SpringExtension::class)
@Import(LocalValidatorFactoryBean::class)
abstract class ValidationTest<T : Any> {
  @Suppress("SpringJavaAutowiredMembersInspection")
  @Autowired
  protected lateinit var validator: Validator

  protected fun assertValid(case: ValidationTestCase<T>) {
    val result = validator.validate(case.subject)
    assertThat(result)
      .describedAs("validating ${case.name} on ${case.subject}")
      .isEmpty()
  }

  protected fun assertInvalid(case: ValidationTestCase<T>) {
    val result = validator.validate(case.subject)
    val paths = result.map {
      val property = it.propertyPath.toString()
      if (property == "") it.message else property
    }.distinct()
    val messages = result.joinToString(", ") { it.message }
    val described = "validating ${case.name} on ${case.subject} -> $messages"

    if (case.strict) {
      assertThat(paths).describedAs(described).containsOnly(*case.invalidPaths.toTypedArray())
    } else {
      assertThat(paths).describedAs(described).contains(*case.invalidPaths.toTypedArray())
    }
  }
}
