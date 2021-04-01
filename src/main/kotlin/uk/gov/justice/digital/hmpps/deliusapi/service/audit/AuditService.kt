package uk.gov.justice.digital.hmpps.deliusapi.service.audit

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.security.SecurityUserContext
import java.time.LocalDateTime

@Service
class AuditService(
  private val auditedInteractionRepository: AuditedInteractionRepository,
  private val businessInteractionRepository: BusinessInteractionRepository,
  private val securityUserContext: SecurityUserContext,
) {

  fun successfulInteraction(interaction: AuditableInteraction, context: AuditContext) =
    createAuditedInteraction(interaction, true, context)

  fun failedInteraction(interaction: AuditableInteraction, context: AuditContext) =
    createAuditedInteraction(interaction, false, context)

  fun createAuditedInteraction(interaction: AuditableInteraction, success: Boolean, context: AuditContext) {
    val parameterMap = mapOf(
      AuditParameter.OFFENDER_ID to (context.offenderId ?: -1),
      AuditParameter.NSI_ID to (context.nsiId ?: -1),
      AuditParameter.CONTACT_ID to (context.contactId ?: -1),
      AuditParameter.PROVIDER_ID to (context.providerId ?: -1),
    ).filter { e -> e.value > 0 }

    val userId = securityUserContext.getCurrentDeliusUserId()

    createAuditedInteraction(
      userId,
      interaction,
      parameterMap.mapValues { it.value.toString() },
      success
    )
  }

  fun createAuditedInteraction(
    userId: Long,
    interaction: AuditableInteraction,
    parameters: Map<AuditParameter, String>,
    success: Boolean
  ) {

    if (parameters.isEmpty()) {
      throw BadRequestException("No audit parameters provided")
    }

    val businessInteraction = businessInteractionRepository.findFirstByCode(interaction.code)
      ?: throw BadRequestException("Business Interaction with code ${interaction.code} does not exist")

    if (!isInteractionAuditable(businessInteraction.enabledDate)) {
      return
    }

    val auditedInteraction = AuditedInteraction(
      null,
      success,
      formatInteractionParameters(parameters),
      businessInteraction,
      userId
    )

    auditedInteractionRepository.saveAndFlush(auditedInteraction)
  }

  private fun formatInteractionParameters(parameters: Map<AuditParameter, String>): String =
    parameters.map { (key, value) -> "${key.code}='$value'" }.joinToString(", ")

  private fun isInteractionAuditable(enabledDate: LocalDateTime?) =
    enabledDate != null && LocalDateTime.now().isAfter(enabledDate)
}
