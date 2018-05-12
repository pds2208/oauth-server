package gov.uk.ons.registers.microservice.config

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.codec.Base64
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.AccessTokenConverter
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util


class CARemoteTokenServices() extends ResourceServerTokenServices {

  private var restTemplate : RestTemplate = new RestTemplate

  restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
    @throws[IOException]
    override // Ignore 400
    def handleError(response: ClientHttpResponse): Unit = {
      if (response.getRawStatusCode != 400) super.handleError(response)
    }
  })

  final protected val logger = LogFactory.getLog(getClass)

  private var checkTokenEndpointUrl : String = _
  private var clientId : String = _
  private var clientSecret : String = _
  private var tokenName : String = "token"
  private var scope : Array[String] = _
  private var grantType : String = "client_credentials"
  private var tokenConverter : AccessTokenConverter = new DefaultAccessTokenConverter

  def setRestTemplate(restTemplate: RestTemplate): Unit = {
    this.restTemplate = restTemplate
  }

  def setCheckTokenEndpointUrl(checkTokenEndpointUrl: String): Unit = {
    this.checkTokenEndpointUrl = checkTokenEndpointUrl
  }

  def setClientId(clientId: String): Unit = {
    this.clientId = clientId
  }

  def setClientSecret(clientSecret: String): Unit = {
    this.clientSecret = clientSecret
  }

  def setAccessTokenConverter(accessTokenConverter: AccessTokenConverter): Unit = {
    this.tokenConverter = accessTokenConverter
  }

  def setTokenName(tokenName: String): Unit = {
    this.tokenName = tokenName
  }

  def getScope: Array[String] = scope

  def setScope(scope: Array[String]): Unit = {
    this.scope = scope
  }

  def getGrantType: String = grantType

  def setGrantType(grantType: String): Unit = {
    this.grantType = grantType
  }

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
    val map = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity[MultiValueMap[String, String]](formData, headers), classOf[util.Map[_, _]]).getBody
    @SuppressWarnings(Array("unchecked"))
    val result = map
    result
  }
}
