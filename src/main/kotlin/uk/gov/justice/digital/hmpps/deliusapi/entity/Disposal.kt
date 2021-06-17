package uk.gov.justice.digital.hmpps.deliusapi.entity

import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "DISPOSAL")
class Disposal(
  @Id
  @Column(name = "DISPOSAL_ID")
  var id: Long,

  @JoinColumn(name = "DISPOSAL_ID")
  @OneToMany
  var requirements: MutableList<Requirement>? = null,

  @JoinColumn(name = "DISPOSAL_TYPE_ID")
  @ManyToOne
  var type: DisposalType? = null,

  @Column(name = "DISPOSAL_DATE", nullable = false)
  var date: LocalDate,
)
