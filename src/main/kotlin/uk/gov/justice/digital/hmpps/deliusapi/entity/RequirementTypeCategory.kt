package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "R_RQMNT_TYPE_MAIN_CATEGORY")
class RequirementTypeCategory(
  @Id
  @Column(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
  var id: Long,

  @Column(name = "CODE", length = 20, nullable = false)
  var code: String,

  @ManyToMany
  @JoinTable(
    name = "R_RQMNT_NSI_TYPE",
    joinColumns = [JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "NSI_TYPE_ID", nullable = false)]
  )
  var nsiTypes: List<NsiType>? = null
) {
  companion object {
    const val RAR_REQUIREMENT_TYPE_CATEGORY_CODE = "F"
  }
}
