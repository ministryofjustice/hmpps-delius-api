package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AuthAwareAuthenticationToken(jwt: Jwt, authorities: Collection<GrantedAuthority?>) :
  JwtAuthenticationToken(jwt, authorities) {

  val subject = jwt.subject
}