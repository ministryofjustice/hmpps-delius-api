package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "USER_")
class User(
  @Id
  @Column(name = "USER_ID")
  val id: Long = 0,

  @Column(name = "DISTINGUISHED_NAME")
  val distinguishedName: String? = null,

  @Column(name = "END_DATE")
  val endDate: LocalDate? = null,

  @JoinTable(
    name = "PROBATION_AREA_USER",
    joinColumns = [JoinColumn(name = "USER_ID")],
    inverseJoinColumns = [JoinColumn(name = "PROBATION_AREA_ID")],
  )
  @Where(clause = "SELECTABLE = 'Y'")
  @OneToMany
  val providers: MutableList<Provider> = mutableListOf(),
)
