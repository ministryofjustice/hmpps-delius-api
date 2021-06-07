package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "RQMNT")
@Where(clause = "SOFT_DELETED = 0")
class Requirement(
  @Id
  @Column(name = "RQMNT_ID")
  var id: Long,

  @Column(name = "TERMINATION_DATE")
  var terminationDate: LocalDate? = null,

  @Column(name = "OFFENDER_ID")
  var offenderId: Long,

  @Column(name = "ACTIVE_FLAG", columnDefinition = "NUMBER", nullable = false)
  var active: Boolean,

  @JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
  @ManyToOne
  var typeCategory: RequirementTypeCategory? = null,

  @Column(name = "RAR_COUNT")
  var rarCount: Long? = null,

  @Column(name = "ROW_VERSION")
  @Version
  val version: Long? = null,

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
