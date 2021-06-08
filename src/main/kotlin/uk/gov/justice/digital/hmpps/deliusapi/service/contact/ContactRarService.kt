package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
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
      val requirement = contact.requirement!!
      val requirementRarCount = contactRepository.countRequirementRar(requirement.id)
      requirement.rarCount = requirementRarCount
      requirementRepository.saveAndFlush(requirement)
      log.info("Requirement ${requirement.id} associated with contact ${contact.id} RAR count updated to $requirementRarCount")
    }

    if (contact.nsi?.requirement != null && contact.nsi!!.requirement!!.isRehabilitationActivityRequirement()) {
      val nsi = contact.nsi!!
      val nsiRarCount = contactRepository.countNsiRar(nsi.id)
      nsi.rarCount = nsiRarCount
      nsiRepository.saveAndFlush(nsi)
      log.info("NSI ${nsi.id} associated with contact ${contact.id} RAR count updated to $nsiRarCount")
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
