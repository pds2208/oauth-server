package gov.uk.ons.registers.microservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import scala.beans.BeanProperty

@Configuration
@ConfigurationProperties(prefix="oauth")
class OAuthConfigurationProperties {

  @BeanProperty
  var clientId : String = _

  @BeanProperty
  var clientSecret : String = _

  @BeanProperty
  var accessTokenUri : String = _

  @BeanProperty
  var grantType : String = _

  @BeanProperty
  var scope : String = _

}
