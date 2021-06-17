package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "EVENT")
@Where(clause = "SOFT_DELETED = 0")
class Event(
  @Id
  @Column(name = "EVENT_ID")
  var id: Long,

  @Column(name = "REFERRAL_DATE", nullable = false)
  var referralDate: LocalDate,

  @Column(name = "ACTIVE_FLAG", columnDefinition = "NUMBER", nullable = false)
  var active: Boolean,

  @Column(name = "FTC_COUNT", nullable = false)
  var ftcCount: Long,

  @Column(name = "IN_BREACH", columnDefinition = "NUMBER", nullable = false)
  var inBreach: Boolean,

  @Column(name = "BREACH_END")
  var breachEnd: LocalDate? = null,

  @JoinColumn(name = "EVENT_ID")
  @OneToMany
  var disposals: MutableList<Disposal>? = null,
) {
  var disposal: Disposal?
    get() = disposals?.getOrNull(0)
    set(value) {
      disposals = if (value == null) mutableListOf() else mutableListOf(value)
    }
}
