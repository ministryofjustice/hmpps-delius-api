package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class NewTeam(
  @ApiModelProperty("The provider code")
  @field:NotBlank
  val provider: String,

  @ApiModelProperty("The cluster code")
  @field:NotBlank
  val cluster: String,

  @ApiModelProperty("The local delivery unit code")
  @field:NotBlank
  val ldu: String,

  @ApiModelProperty("The team type code")
  @field:NotBlank
  val type: String,

  @ApiModelProperty("The description for the new team")
  @field:NotBlank
  @field:Size(max = 50)
  val description: String,

  @ApiModelProperty("If the team is an unpaid work team")
  val unpaidWorkTeam: Boolean = false,
)
