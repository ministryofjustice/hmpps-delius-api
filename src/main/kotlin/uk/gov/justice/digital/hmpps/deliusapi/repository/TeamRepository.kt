package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

@Repository
interface TeamRepository : JpaRepository<Team, Long> {
  fun findByCodeAndProviderCode(code: String, providerCode: String): Team?

  fun findByProviderCode(providerCode: String): List<Team>
}

fun TeamRepository.findByCodeAndProviderCodeOrBadRequest(code: String, providerCode: String): Team =
  this.findByCodeAndProviderCode(code, providerCode) ?: throw BadRequestException("Team '$code' in provider '$providerCode' does not exist")
