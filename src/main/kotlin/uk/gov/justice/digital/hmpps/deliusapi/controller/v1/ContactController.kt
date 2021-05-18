package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.deliusapi.controller.extensions.applyPatch
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ReplaceContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.ContactService
import uk.gov.justice.digital.hmpps.deliusapi.validation.validOrThrow
import javax.validation.Valid
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Api(tags = ["Contact v1"], description = "Contact API")
@RestController
@RequestMapping(value = ["v1/contact"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactController(
  private val service: ContactService,
  private val objectMapper: ObjectMapper,
  private val validator: Validator,
) {

  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Creates a new contact",
    response = ContactDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 201,
        message = "The contact has been successfully created.",
        response = ContactDto::class
      )
    ]
  )
  fun createContact(@NotNull @Valid @RequestBody body: NewContact): ResponseEntity<ContactDto> {
    return status(HttpStatus.CREATED).body(service.createContact(body))
  }

  @PatchMapping("{id}", consumes = ["application/json-patch+json", MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Patches an existing contact by id",
    response = ContactDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "The contact was successfully patched.",
        response = ContactDto::class
      )
    ]
  )
  fun patchContact(
    @PathVariable @Positive id: Long,
    @RequestBody patch: JsonPatch
  ): ContactDto {
    val update = service.getUpdateContact(id)
    val patchedUpdate = objectMapper.applyPatch("contact", patch, update)
    validator.validOrThrow(patchedUpdate)
    return service.updateContact(id, patchedUpdate)
  }

  @DeleteMapping("{id}")
  @ApiOperation("Deletes an existing contact by id")
  fun deleteContact(@PathVariable @Positive id: Long) {
    service.deleteContact(id)
  }

  @PostMapping("{id}/replace")
  @ApiOperation("Replace/reschedule an existing contact")
  @ApiResponses(
    value = [
      ApiResponse(
        code = 201,
        message = "The outcome was stored on the existing contact, and a new contact was created with the provided date/times.",
        response = ContactDto::class
      ),
      ApiResponse(
        code = 404,
        message = "No contact exists with that ID"
      )
    ]
  )
  fun replaceContact(
    @PathVariable @Positive id: Long,
    @RequestBody replaceContact: ReplaceContact
  ): ResponseEntity<ContactDto> {
    return status(HttpStatus.CREATED).body(service.replaceContact(id, replaceContact))
  }
}
