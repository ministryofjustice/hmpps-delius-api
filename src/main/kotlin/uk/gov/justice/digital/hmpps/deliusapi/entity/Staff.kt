package uk.gov.justice.digital.hmpps.deliusapi.entity

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
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "STAFF")
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "START_DATE <= CURRENT_DATE AND (END_DATE IS NULL OR END_DATE > CURRENT_DATE)")
class Staff(
  @Id
  @SequenceGenerator(name = "STAFF_ID_GENERATOR", sequenceName = "STAFF_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STAFF_ID_GENERATOR")
  @Column(name = "STAFF_ID")
  var id: Long = 0,

  @Column(name = "OFFICER_CODE", columnDefinition = "CHAR(7)")
  var code: String,

  @Column(name = "FORENAME", length = 35, nullable = false)
  var firstName: String,

  @Column(name = "FORENAME2", length = 35)
  var middleName: String?,

  @Column(name = "SURNAME", length = 35, nullable = false)
  var lastName: String,

  @Column(name = "START_DATE")
  var startDate: LocalDate,

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

  @Column(name = "PRIVATE", columnDefinition = "NUMBER", nullable = false)
  var privateStaff: Boolean = false,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider? = null,

  @OneToMany(
    cascade = [CascadeType.PERSIST],
    mappedBy = "staff",
    orphanRemoval = true
  )
  var teams: MutableList<StaffTeam> = mutableListOf(),
) {
  fun addTeam(team: Team) {
    teams.add(StaffTeam(team = team, staff = this))
  }
}
