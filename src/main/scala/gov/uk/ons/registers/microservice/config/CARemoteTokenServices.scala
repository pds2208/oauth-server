package gov.uk.ons.registers.microservice.config

import java.io.{IOException, UnsupportedEncodingException}
import java.util

import org.apache.commons.logging.LogFactory
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType}
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.codec.Base64
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.{AccessTokenConverter, DefaultAccessTokenConverter, ResourceServerTokenServices}
import org.springframework.util.{LinkedMultiValueMap, MultiValueMap}
import org.springframework.web.client.{DefaultResponseErrorHandler, RestTemplate}

class CARemoteTokenServices() extends ResourceServerTokenServices {

  private var _restTemplate : RestTemplate = new RestTemplate

  _restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
    @throws[IOException]
    override // Ignore 400
    def handleError(response: ClientHttpResponse): Unit = {
      if (response.getRawStatusCode != 400) super.handleError(response)
    }
  })

  private final val logger = LogFactory.getLog(getClass)

  private var _checkTokenEndpointUrl : String = _
  private var _clientId : String = _
  private var _clientSecret : String = _
  private var _tokenName : String = "token"
  private var _scope : Array[String] = _
  private var _grantType : String = "client_credentials"
  private var _tokenConverter : AccessTokenConverter = new DefaultAccessTokenConverter

  def restTemplate: RestTemplate = _restTemplate
  def restTemplate_= (restTemplate: RestTemplate): Unit = _restTemplate = restTemplate

  def checkTokenEndpointUrl: String = _checkTokenEndpointUrl
  def checkTokenEndpointUrl_= (checkTokenEndpointUrl: String): Unit = _checkTokenEndpointUrl = checkTokenEndpointUrl

  def clientId: String = _clientId
  def clientId_= (clientId: String): Unit = _clientId = clientId

  def clientSecret: String = _clientSecret
  def clientSecret_= (clientSecret: String): Unit = _clientSecret = clientSecret

  def tokenConverter: AccessTokenConverter = _tokenConverter
  def tokenConverter_= (tokenConverter: AccessTokenConverter): Unit = _tokenConverter = tokenConverter

  def tokenName: String = _tokenName
  def tokenName_= (tokenName: String): Unit = _tokenName = tokenName

  def scope: Array[String] = _scope
  def scope_= (scope: Array[String]): Unit = _scope = scope

  def grantType: String = _grantType
  def grantType_= (grantType: String): Unit = _grantType = grantType

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

    val map : util.Map[String, _] = postForMap(checkTokenEndpointUrl, formData, headers)
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

  override def readAccessToken(accessToken: String) = throw new UnsupportedOperationException("Not supported: read access token")

  private def getAuthorizationHeader(clientId: String, clientSecret: String) = {
    if (clientId == null || clientSecret == null)
      logger.warn("Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.")

    val creds = String.format("%s:%s", clientId, clientSecret)

    try
      "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")))
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalStateException("Could not convert String")
    }
  }

  private def postForMap(path: String, formData: MultiValueMap[String, String], headers: HttpHeaders) = {
    if (headers.getContentType == null)
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

    @SuppressWarnings(Array("rawtypes"))
    val map = restTemplate.exchange(path, HttpMethod.POST,
      new HttpEntity[MultiValueMap[String, String]](formData, headers), classOf[util.Map[_, _]]).getBody
    map
  }
}
