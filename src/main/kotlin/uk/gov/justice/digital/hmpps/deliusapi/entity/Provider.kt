package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "PROBATION_AREA")
class Provider(
  @Id
  @Column(name = "PROBATION_AREA_ID")
  var id: Long,

  @Column(name = "CODE", columnDefinition = "CHAR(3)")
  var code: String,

  @Column(name = "SELECTABLE", nullable = false)
  @Type(type = "yes_no")
  var selectable: Boolean = false,

  @Column(name = "DESCRIPTION", length = 60, nullable = false)
  var description: String,

  @OneToMany
  @JoinColumn(name = "PROBATION_AREA_ID")
  var teams: List<Team>? = null,

  @Column(name = "PRIVATE")
  var privateTrust: Boolean = false
)
