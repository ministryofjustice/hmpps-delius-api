package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "TEAM")
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "START_DATE <= CURRENT_DATE AND (END_DATE IS NULL OR END_DATE > CURRENT_DATE)")
class Team(
  @Id
  @Column(name = "TEAM_ID")
  @GeneratedValue(generator = "TEAM_SEQ")
  @SequenceGenerator(name = "TEAM_SEQ", sequenceName = "TEAM_ID_SEQ", allocationSize = 1)
  var id: Long = 0,

  @Column(name = "CODE", columnDefinition = "CHAR(6)")
  var code: String,

  @Column(name = "DESCRIPTION", length = 50, nullable = false)
  var description: String,

  @Column(name = "START_DATE")
  var startDate: LocalDate,

  @Column(name = "END_DATE")
  var endDate: LocalDate? = null,

  @OneToMany(
    cascade = [CascadeType.PERSIST],
    mappedBy = "team",
    orphanRemoval = true
  )
  var staff: MutableList<StaffTeam> = mutableListOf(),

  @ManyToMany
  @JoinTable(
    name = "TEAM_OFFICE_LOCATION",
    joinColumns = [JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")],
    inverseJoinColumns = [JoinColumn(name = "OFFICE_LOCATION_ID", referencedColumnName = "OFFICE_LOCATION_ID")],
  )
  var officeLocations: List<OfficeLocation>? = null,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LOCAL_DELIVERY_UNIT_ID")
  var teamType: TeamType,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "DISTRICT_ID", nullable = false)
  var localDeliveryUnit: LocalDeliveryUnit,

  @Column(name = "PRIVATE")
  var privateTeam: Boolean,

  @Column(name = "UNPAID_WORK_TEAM")
  @Type(type = "yes_no")
  var unpaidWorkTeam: Boolean,

  @Column(name = "ROW_VERSION", nullable = false)
  @Version
  var rowVersion: Long = 1,

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: LocalDateTime? = null,

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: LocalDateTime? = null,

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long = 0,

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long = 0,
) {
  fun addStaff(staff: Staff) {
    this.staff.add(StaffTeam(staff = staff, team = this))
  }
}
