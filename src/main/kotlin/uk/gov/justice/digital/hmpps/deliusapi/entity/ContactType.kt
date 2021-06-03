package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_TYPE")
class ContactType(
  @Id
  @Column(name = "CONTACT_TYPE_ID")
  var id: Long,

  @Column(name = "CODE", nullable = false)
  var code: String,

  @Column(name = "DESCRIPTION", length = 4000, nullable = false)
  var description: String,

  @Column(name = "SELECTABLE", nullable = false)
  @Type(type = "yes_no")
  var selectable: Boolean,

  @Column(name = "SPG_OVERRIDE", columnDefinition = "NUMBER")
  var spgOverride: Boolean?,

  @Column(name = "CONTACT_ALERT_FLAG")
  @Type(type = "yes_no")
  var alertFlag: Boolean,

  @Column(name = "CONTACT_OUTCOME_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var outcomeFlag: YesNoBoth,

  @Column(name = "CONTACT_LOCATION_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var locationFlag: YesNoBoth,

  @Column(name = "ATTENDANCE_CONTACT")
  @Type(type = "yes_no")
  var attendanceContact: Boolean,

  @Column(name = "RECORDED_HOURS_CREDITED")
  @Type(type = "yes_no")
  var recordedHoursCredited: Boolean,

  @Column(name = "CJA_ORDERS", nullable = false, length = 1)
  @Type(type = "yes_no")
  var cjaOrderLevel: Boolean,

  @Column(name = "LEGACY_ORDERS", nullable = false, length = 1)
  @Type(type = "yes_no")
  var legacyOrderLevel: Boolean,

  @Column(name = "OFFENDER_LEVEL_CONTACT", nullable = false, length = 1)
  @Type(type = "yes_no")
  var wholeOrderLevel: Boolean,

  @Column(name = "OFFENDER_EVENT_0", nullable = false, length = 1)
  @Type(type = "yes_no")
  var offenderLevel: Boolean,

  /**
   * This is used in Delius to populate the list of available contact types on the schedule future appointments feature.
   * This should be used when checking if a contact type is appropriate for a logical appointment operation e.g. booking recurring, cancelling.
   */
  @Column(name = "FUTURE_SCHEDULED_CONTACTS_FLAG", nullable = false, length = 1)
  @Type(type = "yes_no")
  var scheduleFutureAppointments: Boolean,

  @Column(name = "NATIONAL_STANDARDS_CONTACT", nullable = false, length = 1)
  @Type(type = "yes_no")
  var nationalStandardsContact: Boolean,

  @Column(name = "EDITABLE", length = 1)
  @Type(type = "yes_no")
  var editable: Boolean?,

  @Column(name = "DEFAULT_HEADINGS")
  @Lob
  var defaultHeadings: String?,

  @Column(name = "RAR_ACTIVITY", length = 1)
  @Type(type = "yes_no")
  var rarActivityRecorded: Boolean?,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_OUTCOME",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID", nullable = false)],
  )
  var outcomeTypes: List<ContactOutcomeType>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_CON_TYPE_REQ_TYPE_MAINCAT",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID", nullable = false)]
  )
  var requirementTypeCategories: List<RequirementTypeCategory>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_NSI_TYPE",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "NSI_TYPE_ID", nullable = false)],
  )
  var nsiTypes: List<NsiType>? = null,
)
