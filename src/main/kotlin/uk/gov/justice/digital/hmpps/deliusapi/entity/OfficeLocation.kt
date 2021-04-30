package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Office location.
 * Note: we are not checking start date in the filter as neither does Delius
 */
@Entity
@Table(name = "OFFICE_LOCATION")
@Where(clause = "END_DATE IS NULL OR END_DATE > CURRENT_DATE")
class OfficeLocation(
  @Id
  @Column(name = "OFFICE_LOCATION_ID")
  var id: Long,

  @Column(name = "CODE", columnDefinition = "CHAR(7)")
  var code: String,

  @Column(name = "DESCRIPTION", length = 50, nullable = false)
  var description: String,
)
