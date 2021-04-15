package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class NewStaff(
  @ApiModelProperty("The lastname of the staff member")
  @field:NotBlank
  @field:Size(max = 35)
  val lastName: String,

  @ApiModelProperty("The firstname of the staff member")
  @field:NotBlank
  @field:Size(max = 35)
  val firstName: String,

  @ApiModelProperty("The provider code of the staff member")
  @field:NotBlank
  @ProviderCode
  val provider: String,

  @field:NotEmpty
  val teams: List<String>,
)
