package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff

@Repository
interface StaffRepository : JpaRepository<Staff, Long> {
  fun findByCode(code: String): Staff
}
