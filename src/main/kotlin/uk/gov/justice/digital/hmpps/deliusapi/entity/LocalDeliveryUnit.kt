package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "DISTRICT")
class LocalDeliveryUnit(
  @Id
  @Column(name = "DISTRICT_ID")
  var id: Long = 0,

  @JoinColumn(name = "BOROUGH_ID")
  @ManyToOne
  var cluster: Cluster,

  @OneToMany
  @JoinColumn(name = "DISTRICT_ID")
  var teams: List<Team>,

  var code: String,

  var description: String,
)
