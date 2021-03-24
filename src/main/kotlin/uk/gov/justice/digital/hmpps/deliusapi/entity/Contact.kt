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
import java.time.LocalTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
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
@Table(name = "CONTACT")
@Where(clause = "SOFT_DELETED = 0")
class Contact(
  @Id
  @SequenceGenerator(name = "CONTACT_ID_GENERATOR", sequenceName = "CONTACT_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_ID_GENERATOR")
  @Column(name = "CONTACT_ID", nullable = false)
  var id: Long = 0,

  @Column(name = "CONTACT_DATE", nullable = false)
  var date: LocalDate,

  @Column(name = "ATTENDED")
  @Type(type = "yes_no")
  var attended: Boolean? = null,

  @Column(name = "COMPLIED")
  @Type(type = "yes_no")
  var complied: Boolean? = null,

  @JoinColumn(name = "OFFENDER_ID", nullable = false)
  @ManyToOne
  var offender: Offender,

  @JoinColumn(name = "NSI_ID")
  @ManyToOne
  var nsi: Nsi? = null,

  @Column(name = "CONTACT_START_TIME")
  var startTime: LocalTime? = null,

  @Column(name = "CONTACT_END_TIME")
  var endTime: LocalTime? = null,

  @Column(name = "HOURS_CREDITED", columnDefinition = "NUMBER")
  var hoursCredited: Double? = null,

  @Column(name = "NOTES")
  @Lob
  var notes: String? = null,

  @JoinColumn(name = "STAFF_ID")
  @ManyToOne
  var staff: Staff? = null,

  @JoinColumn(name = "TEAM_ID")
  @ManyToOne
  var team: Team? = null,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

  @Column(name = "PARTITION_AREA_ID", nullable = false)
  var partitionAreaId: Long = 0,

  @JoinColumn(name = "OFFICE_LOCATION_ID")
  @ManyToOne
  var officeLocation: OfficeLocation? = null,

  @Column(name = "ROW_VERSION", nullable = false)
  @Version
  var rowVersion: Long = 1,

  @Column(name = "ALERT_ACTIVE")
  @Type(type = "yes_no")
  var alert: Boolean? = false,

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: LocalDateTime? = null,

  @Column(name = "SENSITIVE")
  @Type(type = "yes_no")
  var sensitive: Boolean? = false,

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: LocalDateTime? = null,

  @JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)
  @ManyToOne
  var type: ContactType,

  @JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")
  @ManyToOne
  var outcome: ContactOutcomeType? = null,

  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "contact", orphanRemoval = true)
  var enforcements: MutableList<Enforcement> = mutableListOf(),

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long = 0,

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long = 0,

  @Column(name = "TRUST_PROVIDER_FLAG", nullable = false)
  var trustProviderFlag: Long = 0,

  /**
   * TODO set this from configuration
   */
  @Column(name = "STAFF_EMPLOYEE_ID", nullable = false)
  var staffEmployeeId: Long = 1,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider? = null,

  /**
   * TODO set this from configuration
   */
  @Column(name = "TRUST_PROVIDER_TEAM_ID", nullable = false)
  var teamProviderId: Long = 1,

  @Column(name = "DESCRIPTION")
  var description: String? = null,

  @JoinColumn(name = "EVENT_ID")
  @ManyToOne
  var event: Event? = null,

  @JoinColumn(name = "RQMNT_ID")
  @ManyToOne
  var requirement: Requirement? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LINKED_CONTACT_ID")
  var linkedContact: Contact? = null,

  @OneToMany(mappedBy = "linkedContact", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
  var linkedContacts: MutableList<Contact> = mutableListOf(),
) {

  /**
   * OneToOne abstraction over the OneToMany enforcements
   */
  var enforcement: Enforcement?
    get() = if (enforcements.size > 1)
      throw RuntimeException("Cannot determine which enforcement to use on contact with id '$id'")
    else enforcements.getOrNull(0)
    set(value) {
      enforcements.clear()
      if (value != null) {
        value.contact = this
        enforcements.add(value)
      }
    }
}
