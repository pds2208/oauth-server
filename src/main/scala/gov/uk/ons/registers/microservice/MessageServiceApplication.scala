package gov.uk.ons.registers.microservice

import gov.uk.ons.registers.microservice.config.KeycloakSecurityConfiguration
import gov.uk.ons.registers.microservice.controller.MessageController
import gov.uk.ons.registers.microservice.service.MessageService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

object MessageServiceApplication {
  def main(args: Array[String]) : Unit = {
    SpringApplication.run(classOf[MessageServiceApplication], args :_ *)
  }
}

@SpringBootApplication
@ComponentScan(basePackageClasses = Array(
  classOf[MessageController],
  classOf[MessageService],
  classOf[KeycloakSecurityConfiguration])
)
class MessageServiceApplication {}
