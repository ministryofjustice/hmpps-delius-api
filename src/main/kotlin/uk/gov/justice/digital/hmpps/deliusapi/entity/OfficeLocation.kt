package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "OFFICE_LOCATION")
@Where(clause = "END_DATE IS NULL OR END_DATE > CURRENT_DATE()")
data class OfficeLocation(
  @Id
  @Column(name = "OFFICE_LOCATION_ID")
  var id: Long,

  @Column(name = "CODE", columnDefinition = "CHAR(7)")
  val code: String,

  @ManyToMany
  @JoinTable(
    name = "TEAM_OFFICE_LOCATION",
    joinColumns = [JoinColumn(name = "OFFICE_LOCATION_ID", referencedColumnName = "OFFICE_LOCATION_ID")],
    inverseJoinColumns = [JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")]
  )
  var teams: List<Team>? = null,
)
