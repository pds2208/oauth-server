package gov.uk.ons.registers.microservice.config

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy

@Configuration
@EnableWebSecurity
@Profile(Array("keycloak"))
class KeycloakSecurityConfiguration
    extends KeycloakWebSecurityConfigurerAdapter {

  @Autowired
  @throws[Exception]
  def configureGlobal(auth: AuthenticationManagerBuilder): Unit = {
    val keycloakAuthenticationProvider = super.keycloakAuthenticationProvider
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
      new SimpleAuthorityMapper)
    auth.authenticationProvider(keycloakAuthenticationProvider)
  }

  @Bean
  def KeycloakConfigResolver: KeycloakSpringBootConfigResolver = {
    new KeycloakSpringBootConfigResolver
  }

  @Bean
  override protected def sessionAuthenticationStrategy
    : RegisterSessionAuthenticationStrategy = {
    new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl)
  }

  @throws[Exception]
  override protected def configure(http: HttpSecurity): Unit = {
    super.configure(http)
    http.authorizeRequests
      .antMatchers("/message*")
      .hasRole("user")
      .anyRequest
      .permitAll
  }
}
