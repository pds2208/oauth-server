package gov.uk.ons.registers.microservice.config

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy

@Configuration
@EnableWebSecurity
@Profile(Array("keycloak"))
@ComponentScan(basePackageClasses = Array(classOf[KeycloakSecurityComponents]))
class KeycloakSecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

  @Autowired
  @throws[Exception]
  def configureGlobal(auth: AuthenticationManagerBuilder): Unit = {
    val keycloakAuthenticationProvider = super.keycloakAuthenticationProvider
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper)
    auth.authenticationProvider(keycloakAuthenticationProvider)
  }

  @Bean
  def KeycloakConfigResolver: KeycloakSpringBootConfigResolver = {
    new KeycloakSpringBootConfigResolver
  }

  @Bean
  override protected def sessionAuthenticationStrategy: RegisterSessionAuthenticationStrategy = {
    new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl)
  }

  /**
    * Api request URI and their mapping roles and access are configured in this
    * method.This is method from spring security web configuration Override this
    * method to configure the HttpSecurity.
    *
    */
  @throws[Exception]
  override protected def configure(http: HttpSecurity): Unit = {
    super.configure(http)
    http.authorizeRequests.antMatchers("/message*")
      .hasRole("user")
      .anyRequest.permitAll
  }
}
