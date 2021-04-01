package uk.gov.justice.digital.hmpps.deliusapi.config

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.github.fge.jsonpatch.JsonPatchException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadJsonPatchException
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.ConflictException
import javax.validation.ValidationException

@RestControllerAdvice
class HmppsDeliusApiExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.debug("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(e: MethodArgumentNotValidException): ErrorResponse {
    log.debug("Validation exception", e)
    val errors = e.bindingResult.allErrors.mapNotNull {
      if (it is FieldError) {
        "${it.field} ${it.defaultMessage}"
      } else null
    }.joinToString()
    return ErrorResponse(
      status = BAD_REQUEST,
      userMessage = "Validation failure: $errors",
      developerMessage = e.message
    )
  }

  @ResponseStatus(CONFLICT)
  @ExceptionHandler(ConflictException::class)
  fun handleConflict(e: Exception): ErrorResponse {
    log.debug("Conflict", e)
    return ErrorResponse(
      status = CONFLICT,
      userMessage = "Conflict: ${e.message}",
      developerMessage = e.message
    )
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler(HttpMediaTypeNotSupportedException::class, BadRequestException::class)
  fun handleGenericBadRequest(e: Exception): ErrorResponse {
    log.debug("Bad request", e)
    return ErrorResponse(
      status = BAD_REQUEST,
      userMessage = e.message,
      developerMessage = e.message
    )
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException::class, BadJsonPatchException::class)
  fun handleJsonParseException(e: Exception): ErrorResponse {
    log.debug("JSON parse error", e)
    val message = when (val cause = e.cause) {
      is MissingKotlinParameterException -> {
        val messages = cause.path.joinToString(", ") { "${it.fieldName} is required" }
        "Validation failure: $messages"
      }
      is InvalidFormatException -> {
        val messages = cause.path.joinToString(", ") { "${it.fieldName} is not a valid ${cause.targetType.simpleName}" }
        "Validation failure: $messages"
      }
      is JsonPatchException -> "Invalid JSON patch document: ${cause.message}"
      else -> e.message
    }
    return ErrorResponse(
      status = BAD_REQUEST,
      userMessage = message,
      developerMessage = e.message
    )
  }

  @ResponseStatus(UNAUTHORIZED)
  @ExceptionHandler(AccessDeniedException::class)
  fun handleUnauthorized(e: AccessDeniedException): ErrorResponse {
    log.debug("Unauthorized", e)
    return ErrorResponse(
      status = UNAUTHORIZED,
      userMessage = e.message,
      developerMessage = e.message
    )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
