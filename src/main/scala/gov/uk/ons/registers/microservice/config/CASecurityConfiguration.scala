package gov.uk.ons.registers.microservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.config.annotation.web.configuration.{EnableResourceServer, ResourceServerConfigurerAdapter}

@Configuration
@EnableResourceServer
class CASecurityConfiguration extends ResourceServerConfigurerAdapter {

  @Value("${oauth.clientId}")
  var clientId : String = _

  @Value("${oauth.clientSecret}")
  var clientSecret : String = _

  @Value("${oauth.accessTokenUri}")
  var accessTokenUri : String = _

  @Value("${oauth.scope}")
  var accessScope : String = _

  @Value("${oauth.grantType}")
  var grantType : String = "client_credentials"

  @throws[Exception]
  override def configure(http: HttpSecurity): Unit = {
    http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and
      .authorizeRequests
      .antMatchers("/message")
      .access("#oauth2.hasScope('read')")     // require 'read' scope to access /message URL
  }

  @Primary
  @Bean
  def tokenServices : CARemoteTokenServices = {
    val tokenService = new CARemoteTokenServices

    val l : Array[String] = accessScope.split(",").map(_.trim)
    //val m = JavaConverters.seqAsJavaList(l)

    //import collection.JavaConversions._
    //val m: java.util.List[String] = l.toSeq

    tokenService.checkTokenEndpointUrl = accessTokenUri
    tokenService.clientId = clientId
    tokenService.clientSecret = clientSecret
    tokenService.scope = l // was m
    tokenService.grantType = grantType
    tokenService
  }

}