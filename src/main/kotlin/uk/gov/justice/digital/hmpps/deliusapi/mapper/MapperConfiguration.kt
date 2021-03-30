package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfiguration {
  @Bean
  fun contactMapper(): ContactMapper = ContactMapper.INSTANCE

  @Bean
  fun nsiMapper(): NsiMapper = NsiMapper.INSTANCE

  @Bean
  fun staffMapper(): StaffMapper = StaffMapper.INSTANCE
}
