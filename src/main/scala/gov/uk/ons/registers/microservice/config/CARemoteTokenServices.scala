package gov.uk.ons.registers.microservice.config

import java.io.{IOException, UnsupportedEncodingException}
import java.util

import org.apache.commons.logging.LogFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType}
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.codec.Base64
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.{
  AccessTokenConverter,
  DefaultAccessTokenConverter,
  ResourceServerTokenServices
}
import org.springframework.util.{LinkedMultiValueMap, MultiValueMap}
import org.springframework.web.client.{
  DefaultResponseErrorHandler,
  RestTemplate
}

class CARemoteTokenServices extends ResourceServerTokenServices {

  final val logger = LogFactory.getLog(getClass)

  var checkTokenEndpointUrl: String = _
  var clientId: String = _
  var clientSecret: String = _
  var tokenName: String = "token"
  var scope: Array[String] = _
  var grantType: String = "client_credentials"
  var tokenConverter: AccessTokenConverter = new DefaultAccessTokenConverter
  var restTemplate: RestTemplate = new RestTemplate

  restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
    @throws[IOException]
    override // Ignore 400
    def handleError(response: ClientHttpResponse): Unit = {
      if (response.getRawStatusCode != 400) super.handleError(response)
    }
  })

  @throws[AuthenticationException]
  @throws[InvalidTokenException]
  override def loadAuthentication(accessToken: String): OAuth2Authentication = {
    val formData = new LinkedMultiValueMap[String, String]
    formData.add(tokenName, accessToken)

    for (s <- scope) {
      formData.add("scope", s)
    }

    formData.add("grant_type", grantType)

    val headers = new HttpHeaders
    headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret))

    val map = postForMap(checkTokenEndpointUrl, formData, headers)
        .asInstanceOf[util.Map[String, Any]]

    if (map.containsKey("error")) {
      if (logger.isDebugEnabled)
        logger.debug("check_token returned error: " + map.get("error"))
      throw new InvalidTokenException(accessToken)
    }
    //TO: Check why CA doesn't return active

    // gh-838
    //        if (!Boolean.TRUE.equals(map.get("active"))) {
    //            logger.debug("check_token returned active attribute: " + map.get("active"));
    //            throw new InvalidTokenException(accessToken);
    //        }

    tokenConverter.extractAuthentication(map)
  }

  override def readAccessToken(accessToken: String) =
    throw new UnsupportedOperationException("Not supported: read access token")

  private def getAuthorizationHeader(clientId: String, clientSecret: String) = {
    if (clientId == null || clientSecret == null)
      logger.warn(
        "Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.")

    val creds = String.format("%s:%s", clientId, clientSecret)

    try "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")))
    catch {
      case _: UnsupportedEncodingException =>
        throw new IllegalStateException("Could not convert String")
    }
  }

  private def postForMap(path: String,
                         formData: MultiValueMap[String, String],
                         headers: HttpHeaders) = {
    if (headers.getContentType == null)
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

    val map = restTemplate
      .exchange(
        path,
        HttpMethod.POST,
        new HttpEntity[MultiValueMap[String, String]](formData, headers),
        classOf[util.Map[_, _]])
      .getBody
    map
  }
}
