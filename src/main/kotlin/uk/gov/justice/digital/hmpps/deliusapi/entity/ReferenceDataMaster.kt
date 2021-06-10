package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "R_REFERENCE_DATA_MASTER")
class ReferenceDataMaster(
  @Id
  @Column(name = "REFERENCE_DATA_MASTER_ID")
  var id: Long,

  @Column(name = "CODE_SET_NAME", length = 100, nullable = false)
  var code: String,

  @JoinColumn(name = "REFERENCE_DATA_MASTER_ID")
  @OneToMany
  var standardReferences: MutableList<StandardReference>? = null,
)
