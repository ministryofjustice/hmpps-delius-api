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
@Table(name = "TEAM")
@Where(clause = "START_DATE <= CURRENT_DATE AND (END_DATE IS NULL OR END_DATE > CURRENT_DATE)")
class Team(
  @Id
  @Column(name = "TEAM_ID")
  var id: Long = 0,

  @Column(name = "CODE", columnDefinition = "CHAR(6)")
  var code: String,

  @Column(name = "DESCRIPTION", length = 50, nullable = false)
  var description: String,

  @ManyToMany
  @JoinTable(
    name = "STAFF_TEAM",
    joinColumns = [JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")],
    inverseJoinColumns = [JoinColumn(name = "STAFF_ID", referencedColumnName = "STAFF_ID")],
  )
  var staff: List<Staff>? = null,

  @ManyToMany
  @JoinTable(
    name = "TEAM_OFFICE_LOCATION",
    joinColumns = [JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")],
    inverseJoinColumns = [JoinColumn(name = "OFFICE_LOCATION_ID", referencedColumnName = "OFFICE_LOCATION_ID")],
  )
  var officeLocations: List<OfficeLocation>? = null,
)
