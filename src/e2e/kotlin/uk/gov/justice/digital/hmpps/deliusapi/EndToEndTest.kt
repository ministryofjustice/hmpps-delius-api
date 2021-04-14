package uk.gov.justice.digital.hmpps.deliusapi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.openapitools.client.infrastructure.ApiClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.deliusapi.client.api.ContactV1Api
import uk.gov.justice.digital.hmpps.deliusapi.client.api.NSIV1Api
import uk.gov.justice.digital.hmpps.deliusapi.client.api.OperationHandlerApi
import uk.gov.justice.digital.hmpps.deliusapi.client.api.StaffV1Api
import uk.gov.justice.digital.hmpps.deliusapi.client.api.TeamV1Api
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactSelector
import uk.gov.justice.digital.hmpps.deliusapi.config.EndToEndTestActiveProfilesResolver
import uk.gov.justice.digital.hmpps.deliusapi.config.EndToEndTestConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiSelector
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.config.newNsi
import java.time.LocalDateTime

typealias CopyFn<T> = (x: T) -> T

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(resolver = EndToEndTestActiveProfilesResolver::class)
abstract class EndToEndTest {
  protected val logger = LoggerFactory.getLogger(EndToEndTest::class.java)

  @Autowired protected lateinit var configuration: EndToEndTestConfiguration

  protected val contactV1 by lazy { ContactV1Api(configuration.url) }
  protected val nsiV1 by lazy { NSIV1Api(configuration.url) }
  protected val staffV1 by lazy { StaffV1Api(configuration.url) }
  protected val teamV1 by lazy { TeamV1Api(configuration.url) }
  protected val instrumentationApi by lazy { OperationHandlerApi(configuration.url) }

  @BeforeEach
  fun setupAuth() {
    ApiClient.accessToken = getToken()

    // API should be healthy
    assertDoesNotThrow("api should be up") {
      instrumentationApi.safely { it.getHealth(null) }
    }
  }

  protected fun databaseAssertEnabled(): Boolean {
    if (configuration.databaseAssert) {
      return true
    }
    logger.warn("Skipping database assertions")
    return false
  }

  protected fun havingExistingNsi(select: NsiSelector, copy: CopyFn<NewNsi>? = null): NsiDto {
    val selected = configuration.newNsi(select)
    val request = copy?.invoke(selected) ?: selected
    return nsiV1.safely { it.createNsi(request) }
  }

  protected fun havingExistingContact(select: ContactSelector, copy: CopyFn<NewContact>? = null): ContactDto {
    val selected = configuration.newContact(select)
    val request = copy?.invoke(selected) ?: selected
    return contactV1.safely { it.createContact(request) }
  }

  private fun getToken(): String {
    if (CACHED_TOKEN?.expired() == false) {
      return CACHED_TOKEN!!.accessToken
    }

    val client = WebClient.builder()
      .baseUrl(configuration.oauth.url)
      .defaultHeaders {
        it.accept = listOf(MediaType.APPLICATION_JSON)
        it.contentType = MediaType.APPLICATION_JSON
        it.setBasicAuth(configuration.oauth.clientId, configuration.oauth.clientSecret)
      }
      .build()

    CACHED_TOKEN = client
      .post()
      .uri { it.path("/oauth/token").queryParam("grant_type", "client_credentials").build() }
      .retrieve()
      .bodyToMono(TokenResponse::class.java)
      .block()

    return CACHED_TOKEN!!.accessToken
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long,
  ) {
    val expiresAt: LocalDateTime =
      LocalDateTime.now()
        .plusSeconds(expiresIn)
        .minusMinutes(1) // some consumer grace time

    fun expired() = expiresAt >= LocalDateTime.now()
  }

  companion object {
    private var CACHED_TOKEN: TokenResponse? = null
  }
}
