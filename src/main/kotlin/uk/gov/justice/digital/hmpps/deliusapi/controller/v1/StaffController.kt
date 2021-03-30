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
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.StaffDto
import uk.gov.justice.digital.hmpps.deliusapi.service.staff.StaffService
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(value = ["v1/staff"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StaffController(private val service: StaffService) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Creates a new staff member",
    response = StaffDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 201,
        message = "The staff member has been successfully created.",
        response = StaffDto::class
      )
    ]
  )
  fun create(@NotNull @Valid @RequestBody body: NewStaff): ResponseEntity<StaffDto> {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createStaff(body))
  }
}
