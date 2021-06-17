package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "R_NSI_TYPE")
@Where(clause = "SELECTABLE = 'Y'")
class NsiType(
  @Id
  @Column(name = "NSI_TYPE_ID", nullable = false)
  var id: Long = 0,

  @Column(name = "CODE", nullable = false)
  var code: String,

  @Column(name = "OFFENDER_LEVEL", columnDefinition = "NUMBER", nullable = false)
  var offenderLevel: Boolean = false,

  @Column(name = "EVENT_LEVEL", columnDefinition = "NUMBER", nullable = false)
  var eventLevel: Boolean = false,

  @Column(name = "ALLOW_ACTIVE_DUPLICATES", columnDefinition = "NUMBER", nullable = false)
  var allowActiveDuplicates: Boolean = false,

  @Column(name = "ALLOW_INACTIVE_DUPLICATES", columnDefinition = "NUMBER", nullable = false)
  var allowInactiveDuplicates: Boolean = false,

  @JoinColumn(name = "UNITS_ID")
  @ManyToOne
  var units: StandardReference? = null,

  @Column(name = "MINIMUM_LENGTH")
  var minimumLength: Long? = null,

  @Column(name = "MAXIMUM_LENGTH")
  var maximumLength: Long? = null,

  @ManyToMany
  @JoinTable(
    name = "R_NSI_TYPE_SUB_TYPE",
    joinColumns = [JoinColumn(name = "NSI_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "NSI_SUB_TYPE_ID")],
  )
  var subTypes: MutableList<StandardReference>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_NSI_TYPE_STATUS",
    joinColumns = [JoinColumn(name = "NSI_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "NSI_STATUS_ID")],
  )
  var statuses: MutableList<NsiStatus>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_NSI_TYPE_OUTCOME",
    joinColumns = [JoinColumn(name = "NSI_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "NSI_OUTCOME_ID")],
  )
  var outcomes: MutableList<StandardReference>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_NSI_TYPE_PROBATION_AREA",
    joinColumns = [JoinColumn(name = "NSI_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "PROBATION_AREA_ID")],
  )
  @Where(clause = "SELECTABLE = 'Y'")
  var providers: MutableList<Provider>? = null,
)
