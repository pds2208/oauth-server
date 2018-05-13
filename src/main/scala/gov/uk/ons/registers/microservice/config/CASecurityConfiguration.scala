package gov.uk.ons.registers.microservice.config

import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.config.annotation.web.configuration.{EnableResourceServer, ResourceServerConfigurerAdapter}

@Configuration
@EnableResourceServer
class CASecurityConfiguration (@Autowired val config: OAuthConfigurationProperties) extends ResourceServerConfigurerAdapter {

  @throws[Exception]
  override def configure(http: HttpSecurity): Unit = {
    http
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and
      .authorizeRequests
      .antMatchers("/message")
      .access("#oauth2.hasScope('read')") // require 'read' scope to access /message URL
  }

  @Primary
  @Bean
  def tokenServices: CARemoteTokenServices = {
    val tokenService = new CARemoteTokenServices

    val scopes: Array[String] = config.scope
      .split(",")
      .map(_.trim)

    tokenService.checkTokenEndpointUrl = config.accessTokenUri
    tokenService.clientId = config.clientId
    tokenService.clientSecret = config.clientSecret
    tokenService.scope = scopes
    tokenService.grantType = config.grantType
    tokenService
  }

}
