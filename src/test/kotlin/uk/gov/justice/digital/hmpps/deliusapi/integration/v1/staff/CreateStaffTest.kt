package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.staff

import com.nhaarman.mockitokotlin2.doReturn
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.staff.NewStaff
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("test-h2")
class CreateStaffTest : IntegrationTestBase() {

  // Needed due to https://github.com/spring-projects/spring-boot/issues/7033
  @TestConfiguration
  internal class Config {
    @Bean
    fun providerRepositoryPostProcessor(): BeanPostProcessor {
      return ProxiedMockPostProcessor(ProviderRepository::class.java)
    }

    internal class ProxiedMockPostProcessor(private val mockedClass: Class<*>) : BeanPostProcessor {
      @Throws(BeansException::class)
      override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        return if (mockedClass.isInstance(bean)) {
          Mockito.mock(mockedClass, AdditionalAnswers.delegatesTo<Any>(bean))
        } else bean
      }
    }
  }

  @Autowired
  private lateinit var staffRepository: StaffRepository

  @Autowired
  private lateinit var providerRepository: ProviderRepository

  @Test
  fun `creating a staff member`() {
    val request = Fake.validNewStaff()

    doReturn("ABC123").`when`(providerRepository).getNextStaffCode("C00")

    webTestClient.whenCreatingStaff(request)
      // Then it should return successfully
      .expectStatus().isCreated
      .expectBody()
      .shouldReturnCreatedStaff(request)
      // And it should save the entity to the database with the correct details
      .shouldSaveStaff(request)
  }

  protected fun WebTestClient.whenCreatingStaff(request: NewStaff): WebTestClient.ResponseSpec = this
    .post().uri("/v1/staff")
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(request)
    .exchange()

  private fun WebTestClient.BodyContentSpec.shouldReturnCreatedStaff(request: NewStaff): WebTestClient.BodyContentSpec {
    jsonPath("$.lastName").value(Matchers.equalTo(request.lastName))
    jsonPath("$.firstName").value(Matchers.equalTo(request.firstName))
    return this
  }

  private fun WebTestClient.BodyContentSpec.shouldSaveStaff(request: NewStaff): WebTestClient.BodyContentSpec =
    this.jsonPath("$.code").value<String> {
      val savedStaff = staffRepository.findByCode(it)
      assertThat(savedStaff).describedAs("should save entity").isNotNull
      assertThat(savedStaff.firstName).isEqualTo(request.firstName)
      assertThat(savedStaff.firstName).isEqualTo(request.firstName)
      assertThat(savedStaff.provider!!.code).isEqualTo(request.provider)
      assertThat(savedStaff.startDate).isEqualTo(LocalDate.now())
      assertThat(savedStaff.createdDateTime).isCloseTo(LocalDateTime.now(), Assertions.within(1, ChronoUnit.MINUTES))
    }
}
