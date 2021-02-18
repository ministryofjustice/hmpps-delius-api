package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@ReportAsSingleViolation
@NotBlankWhenProvided
@Size(max = 20)
@Pattern(regexp = "^[a-zA-Z0-9]+\$")
annotation class NsiTypeCode(
  val message: String = "must be a valid NSI type code",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
