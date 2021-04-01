package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

@Repository
interface ProviderRepository : JpaRepository<Provider, Long> {
  fun findByCodeAndSelectableIsTrue(code: String): Provider?

  @Query(value = "SELECT spgconfig.getNextStaffReference(probation_area_code_in => ?1) FROM DUAL", nativeQuery = true)
  fun getNextStaffCode(datasetCode: String): String
}

fun ProviderRepository.findByCodeAndSelectableIsTrueOrBadRequest(code: String): Provider =
  this.findByCodeAndSelectableIsTrue(code) ?: throw BadRequestException("Provider '$code' does not exist")
