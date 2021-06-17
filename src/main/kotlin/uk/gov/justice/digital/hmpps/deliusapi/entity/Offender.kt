package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "OFFENDER")
@Where(clause = "SOFT_DELETED = 0")
class Offender(
  @Id
  @Column(name = "OFFENDER_ID")
  var id: Long,

  @Column(name = "CRN", columnDefinition = "CHAR(7)")
  var crn: String,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

  @OneToMany
  @JoinColumn(name = "OFFENDER_ID")
  var events: MutableList<Event>? = null,
)
