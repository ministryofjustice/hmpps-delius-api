package uk.gov.justice.digital.hmpps.deliusapi.service.audit

import kotlin.concurrent.getOrSet

data class AuditContext(
  var offenderId: Long? = null,
  var nsiId: Long? = null,
  var contactId: Long? = null,
  var providerId: Long? = null
) {

  companion object {
    private val threadLocal = ThreadLocal<MutableMap<AuditableInteraction, AuditContext>>()

    fun get(key: AuditableInteraction): AuditContext =
      threadLocal.getOrSet { mutableMapOf() }.getOrPut(key) { AuditContext() }

    fun reset(key: AuditableInteraction) = threadLocal.getOrSet { mutableMapOf() }.put(key, AuditContext())
  }
}
