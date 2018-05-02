package gov.uk.ons.registers.microservice.controller

import gov.uk.ons.registers.microservice.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, ResponseBody}

@Controller
class MessageController @Autowired()(messageService: MessageService) {

  @RequestMapping(path = Array("/message"), method = Array(RequestMethod.GET), produces = Array(MediaType.TEXT_PLAIN_VALUE))
  @ResponseBody
  def handleRequest(): String = {
    "Greetings from a Scala controller, Service message: " + messageService.getMessage
  }

}
