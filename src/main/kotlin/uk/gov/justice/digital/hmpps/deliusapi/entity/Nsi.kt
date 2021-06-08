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
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "NSI")
@Where(clause = "SOFT_DELETED = 0")
class Nsi(
  @Id
  @SequenceGenerator(name = "NSI_ID_GENERATOR", sequenceName = "NSI_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NSI_ID_GENERATOR")
  @Column(name = "NSI_ID", nullable = false)
  var id: Long = 0,

  @JoinColumn(name = "OFFENDER_ID", nullable = false)
  @ManyToOne
  var offender: Offender? = null,

  @JoinColumn(name = "EVENT_ID")
  @ManyToOne
  var event: Event? = null,

  @JoinColumn(name = "NSI_TYPE_ID", nullable = false)
  @ManyToOne
  var type: NsiType,

  @JoinColumn(name = "NSI_SUB_TYPE_ID")
  @ManyToOne
  var subType: StandardReference? = null,

  @Column(name = "LENGTH")
  var length: Long? = null,

  @Column(name = "REFERRAL_DATE", nullable = false)
  var referralDate: LocalDate,

  @Column(name = "EXPECTED_START_DATE")
  var expectedStartDate: LocalDate? = null,

  @Column(name = "EXPECTED_END_DATE")
  var expectedEndDate: LocalDate? = null,

  @Column(name = "ACTUAL_START_DATE")
  var startDate: LocalDate? = null,

  @Column(name = "ACTUAL_END_DATE")
  var endDate: LocalDate? = null,

  @JoinColumn(name = "NSI_STATUS_ID", nullable = false)
  @ManyToOne
  var status: NsiStatus? = null,

  @Column(name = "NSI_STATUS_DATE", nullable = false)
  var statusDate: LocalDateTime,

  @Column(name = "NOTES")
  @Lob
  var notes: String? = null,

  @JoinColumn(name = "NSI_OUTCOME_ID")
  @ManyToOne
  var outcome: StandardReference? = null,

  @Column(name = "ACTIVE_FLAG", columnDefinition = "NUMBER")
  var active: Boolean,

  @Column(name = "PENDING_TRANSFER", columnDefinition = "NUMBER")
  var pendingTransfer: Boolean,

  @JoinColumn(name = "RQMNT_ID")
  @ManyToOne
  var requirement: Requirement? = null,

  @JoinColumn(name = "INTENDED_PROVIDER_ID")
  @ManyToOne
  var intendedProvider: Provider? = null,

  @Column(name = "RAR_COUNT")
  var rarCount: Long? = null,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

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

  @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH], mappedBy = "nsi")
  @Where(clause = "ACTIVE_FLAG = 1")
  var managers: MutableList<NsiManager> = ArrayList(),

  @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH], mappedBy = "nsi")
  var statuses: MutableList<NsiStatusHistory> = ArrayList(),
) {
  var manager: NsiManager?
    get() = managers.getOrNull(0)
    set(value) {
      managers.clear()
      if (value != null) {
        managers.add(value)
      }
    }
}
