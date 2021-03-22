package uk.gov.justice.digital.hmpps.deliusapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.config.wiremock.TokenVerificationExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import java.lang.RuntimeException

const val DEFAULT_INTEGRATION_TEST_USER_NAME = "NationalUser"

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(TokenVerificationExtension::class)
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  protected lateinit var auditedInteractionRepository: AuditedInteractionRepository

  protected var userName = DEFAULT_INTEGRATION_TEST_USER_NAME

  @BeforeEach
  fun beforeEach() {
    userName = DEFAULT_INTEGRATION_TEST_USER_NAME
  }

  protected fun WebTestClient.RequestBodySpec.whenSendingUnauthenticatedRequest(): WebTestClient.ResponseSpec =
    contentType(MediaType.APPLICATION_JSON)
      .bodyValue("{}")
      .exchange()

  protected fun WebTestClient.RequestBodySpec.whenSendingMalformedJson(): WebTestClient.ResponseSpec =
    havingAuthentication()
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue("{,}")
      .exchange()

  protected fun WebTestClient.whenCreatingContact(request: NewContact) = this
    .post().uri("/v1/contact")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()

  protected fun WebTestClient.BodyContentSpec.shouldReturnJsonParseError() =
    jsonPath("$.userMessage").value(startsWith("JSON parse error: "))

  protected fun WebTestClient.BodyContentSpec.shouldReturnValidationError(vararg invalidPaths: String) =
    jsonPath("$.userMessage").value(allOf(invalidPaths.map { containsString(it) }))

  protected fun WebTestClient.BodyContentSpec.shouldReturnAccessDenied() =
    jsonPath("$.userMessage").value(startsWith("Access is denied"))

  protected fun <T : WebTestClient.RequestHeadersSpec<T>> T.havingAuthentication(
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expired: Boolean = false,
    authSource: String = "delius",
    databaseUsername: String? = null
  ): T {
    val token = jwtAuthHelper.createJwt(
      userName,
      scope = scope,
      roles = roles,
      expired = expired,
      authSource = authSource,
      databaseUsername = databaseUsername
    )
    return this.header("Authorization", "Bearer $token")
  }

  protected fun <T> WebTestClient.BodyContentSpec.shouldCreateEntityById(repository: CrudRepository<T, Long>, assertion: (entity: T) -> Unit): WebTestClient.BodyContentSpec =
    this.jsonPath("$.id").value<Any> {
      val id = when (it) {
        is Long -> it
        is Int -> it.toLong()
        else -> throw RuntimeException("unknown id type ${it.javaClass.name}")
      }

      val entity = repository.findByIdOrNull(id)
      assertThat(entity).describedAs("should save entity").isNotNull

      assertion(entity!!)
    }
}
