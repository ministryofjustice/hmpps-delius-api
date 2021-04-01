package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.NewTeam
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.team.TeamDto
import uk.gov.justice.digital.hmpps.deliusapi.service.team.TeamService
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(value = ["v1/team"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TeamController(
  private val service: TeamService
) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Creates a new team",
    response = TeamDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 201,
        message = "The team has been successfully created.",
        response = TeamDto::class
      )
    ]
  )
  fun create(@NotNull @Valid @RequestBody body: NewTeam): ResponseEntity<TeamDto> {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body))
  }
}
