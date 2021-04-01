package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "LOCAL_DELIVERY_UNIT")
class TeamType(
  @Id
  @Column(name = "LOCAL_DELIVERY_UNIT_ID")
  var id: Long,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider,

  var description: String,

  var code: String,
)
