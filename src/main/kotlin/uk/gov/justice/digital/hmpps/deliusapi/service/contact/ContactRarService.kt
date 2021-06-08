package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isRehabilitationActivityRequirement

@Service
class ContactRarService(
  private val contactRepository: ContactRepository,
  private val requirementRepository: RequirementRepository,
  private val nsiRepository: NsiRepository
) {
  @Transactional
  fun updateRarCounts(contact: Contact) {
    log.info("Determining if RAR count updates required for contact ${contact.id}")
    if (contact.requirement != null && contact.requirement!!.isRehabilitationActivityRequirement()) {
      updateRequirementRarCount(contact.requirement!!, contact)
    }

    if (contact.nsi?.requirement != null && contact.nsi!!.requirement!!.isRehabilitationActivityRequirement()) {
      updateNsiRarCount(contact)
      updateRequirementRarCount(contact.nsi!!.requirement!!, contact)
    }
  }

  private fun updateNsiRarCount(contact: Contact) {
    val nsi = contact.nsi!!
    nsi.rarCount = contactRepository.countNsiRar(nsi.id)
    nsiRepository.saveAndFlush(nsi)
    log.info("NSI ${nsi.id} associated with contact ${contact.id} RAR count updated to ${nsi.rarCount}")
  }

  private fun updateRequirementRarCount(
    requirement: Requirement,
    contact: Contact
  ) {
    requirement.rarCount = contactRepository.countRequirementRar(requirement.id)
    requirementRepository.saveAndFlush(requirement)
    log.info("Requirement ${requirement.id} associated with contact ${contact.id} RAR count updated to ${requirement.rarCount}")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
