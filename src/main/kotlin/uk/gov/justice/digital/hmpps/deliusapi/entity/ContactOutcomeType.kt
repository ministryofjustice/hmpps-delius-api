package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_OUTCOME_TYPE")
@Where(clause = "SELECTABLE = 'Y'")
class ContactOutcomeType(
  @Id
  @Column(name = "CONTACT_OUTCOME_TYPE_ID")
  var id: Long,

  @Column(name = "CODE", nullable = false)
  var code: String,

  @Column(name = "DESCRIPTION", length = 50, nullable = false)
  var description: String,

  @Column(name = "OUTCOME_COMPLIANT_ACCEPTABLE", length = 1)
  @Type(type = "yes_no")
  var compliantAcceptable: Boolean?,

  @Column(name = "OUTCOME_ATTENDANCE", length = 1)
  @Type(type = "yes_no")
  var attendance: Boolean?,

  @Column(name = "ACTION_REQUIRED", nullable = false)
  @Type(type = "yes_no")
  var actionRequired: Boolean,

  @Column(name = "ENFORCEABLE", length = 1)
  @Type(type = "yes_no")
  var enforceable: Boolean?,
)
