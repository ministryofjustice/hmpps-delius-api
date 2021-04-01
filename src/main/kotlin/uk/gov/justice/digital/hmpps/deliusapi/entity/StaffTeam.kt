package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Table
import javax.persistence.Version

@Embeddable
data class StaffTeamIdentity(
  @Column(name = "STAFF_ID")
  var staffId: Long? = null,

  @Column(name = "TEAM_ID")
  var teamId: Long? = null,
) : Serializable

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "STAFF_TEAM")
class StaffTeam(
  @EmbeddedId
  var id: StaffTeamIdentity = StaffTeamIdentity(),

  @Version
  @Column(name = "ROW_VERSION")
  var rowVersion: Long? = 0,

  @ManyToOne
  @MapsId("staffId")
  @JoinColumn(name = "STAFF_ID")
  val staff: Staff,

  @ManyToOne
  @MapsId("teamId")
  @JoinColumn(name = "TEAM_ID")
  val team: Team,

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
)
