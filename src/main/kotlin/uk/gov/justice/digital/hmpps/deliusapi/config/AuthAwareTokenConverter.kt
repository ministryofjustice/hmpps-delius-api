package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.entity.User
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.security.DeliusSecurityService

@Component
class AuthAwareTokenConverter(
  private val deliusSecurity: DeliusSecurityService,
) : Converter<Jwt, AbstractAuthenticationToken> {

  @Transactional
  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val user: User = when (jwt.getClaimAsString(ClaimNames.AUTH_SOURCE)) {
      "delius" -> deliusSecurity.getUser(jwt.getClaimAsString(ClaimNames.USER_NAME))
      "none" -> deliusSecurity.getUser(
        jwt.getClaimAsString(ClaimNames.DATABASE_USERNAME)
          ?: throw BadRequestException("Database username required for client credentials")
      )
      else -> throw BadRequestException("Authentication source must be Delius user or client credentials")
    }

    val externalAuthorities = jwt.claims.getOrDefault(ClaimNames.AUTHORITIES, emptyList<String>()) as? Collection<*>
      ?: emptyList<String>()
    val providerAuthorities = user.providers.map { Authorities.PROVIDER + it.code }

    val allAuthorities = (externalAuthorities + providerAuthorities).mapNotNull {
      when (it) {
        is String -> SimpleGrantedAuthority(it)
        else -> null
      }
    }.toSet()

    return AuthAwareAuthenticationToken(jwt, allAuthorities, user.id)
  }
}
