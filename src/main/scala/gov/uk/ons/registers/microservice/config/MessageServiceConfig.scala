package gov.uk.ons.registers.microservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@Component
@ConfigurationProperties("message-service")
case class MessageServiceConfig() {
  @BeanProperty
  var messageKey: String = _
}
