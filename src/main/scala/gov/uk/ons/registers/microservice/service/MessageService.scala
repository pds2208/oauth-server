package gov.uk.ons.registers.microservice.service

import gov.uk.ons.registers.microservice.config.MessageServiceConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MessageService @Autowired()(serviceConfig: MessageServiceConfig) {
  def getMessage: String = {
    s"The service says: ${serviceConfig.messageKey}"
  }
}
