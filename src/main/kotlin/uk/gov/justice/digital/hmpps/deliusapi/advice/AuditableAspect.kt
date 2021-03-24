package uk.gov.justice.digital.hmpps.deliusapi.advice

import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditable(val interaction: AuditableInteraction)

/**
 * Regarding the ordering:
 *
 * By default the transaction runs at the lowest precedence.
 * Running at one higher means our audit aspect runs before the transaction on the way in,
 * and after on the way out, meaning the audit is unaffected by transaction rollbacks due to errors.
 *
 * This could be any value lower than Integer.MAX_VALUE (Ordered.LOWEST_PRECEDENCE) and would work.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class AuditableAspect(private val auditService: AuditService) {
  companion object {
    private const val JOIN_POINT = "@annotation(auditable)"
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Before(JOIN_POINT)
  fun beforeAudit(auditable: Auditable) = AuditContext.reset(auditable.interaction)

  @AfterReturning(JOIN_POINT)
  fun auditSuccess(auditable: Auditable) = audit(auditable, true)

  @AfterThrowing(JOIN_POINT, throwing = "exception")
  fun auditFailure(auditable: Auditable, exception: Throwable) {
    // do not audit bad requests, this is what delius does
    if (exception !is BadRequestException) {
      audit(auditable, false)
    }
  }

  private fun audit(auditable: Auditable, success: Boolean) {
    val context = AuditContext.get(auditable.interaction)
    if (context == AuditContext()) {
      val message = "Cannot audit {} '${auditable.interaction}' interaction with empty context"
      if (success) {
        throw RuntimeException(message.replace("{}", "successful"))
      } else {
        log.warn(message, "failed")
      }
    } else {
      auditService.createAuditedInteraction(auditable.interaction, success, context)
    }
  }
}
