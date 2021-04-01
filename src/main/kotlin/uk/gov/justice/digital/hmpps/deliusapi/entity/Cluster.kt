package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "BOROUGH")
@Where(clause = "SELECTABLE = 'Y'")
class Cluster(
  @Id
  @Column(name = "BOROUGH_ID")
  var id: Long,

  var code: String,

  var description: String,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider? = null,

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "cluster")
  var localDeliveryUnits: MutableList<LocalDeliveryUnit> = mutableListOf()
)
