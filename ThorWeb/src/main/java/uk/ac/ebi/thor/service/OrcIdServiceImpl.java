package uk.ac.ebi.thor.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import uk.ac.ebi.thor.exception.NonAuthorizedThorException;
import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.helper.OrcIdXmlHelper;

import java.io.IOException;
import java.util.Arrays;

/**
 * Class to consume the public/member Rest API provided by ORCID.
 * @author Guilherme Formaggio
 */
public class OrcIdServiceImpl implements OrcIdService {

  private static final Logger LOGGER = LogManager.getLogger(OrcIdServiceImpl.class);

  private RestTemplate orcidRestTemplate;

  private String orcidSearchUrl;
  private String orcidBioUrl;
  private String orcidInfoUrl;
  private String orcidWorksUrl;

  private JsonFactory jsonFactory;
  private HttpTransport httpTransport;

  /**
   * Search the Web Service from ORCID Registry public API.
   * @param keyword
   *        keyword to search ORCID records
   * @return Results as JSON format
   */
  @Override
  public String searchOrcIdsAllRecords(String keyword) {
    LOGGER.trace("Starting searchOrcIdsAllRecords by keyword: {}", keyword);

    //variable to return the JSON response
    String result = "";

    //Headers to request JSON format response instead of xml
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

    //Rest webservice endpoint with parameters
    String url = orcidSearchUrl + "?q=" + keyword;

    //calls the web service 
    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
    ResponseEntity<String> response = orcidRestTemplate.exchange(url, HttpMethod.GET, entity,
        String.class);

    //if no errors occured, extract the JSON response
    if (HttpStatus.OK.equals(response.getStatusCode())) {
      result = response.getBody();
    } else {
      LOGGER.info("ORCID Search Web Service Unsuccessfull! \nUrl:{} \nSTATUS:{} \nBODY:{}", url,
          response.getStatusCode(), response.getBody());
    }

    LOGGER.trace("Exiting searchOrcIdsAllRecords:\n {}", result);
    return result;
  }

  @Override
  public String getOrcIdBio(String orcId) {
    LOGGER.trace("Starting getOrcIdBio({})", orcId);

    //variable to return the JSON response
    String result = "";

    //Headers to request JSON format response instead of xml
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

    //Rest webservice endpoint with parameters
    String url = String.format(orcidBioUrl, orcId);

    //calls the web service 
    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
    ResponseEntity<String> response = orcidRestTemplate.exchange(url, HttpMethod.GET, entity,
        String.class);

    //if no errors occured, extract the JSON response
    if (HttpStatus.OK.equals(response.getStatusCode())) {
      result = response.getBody();
    } else {
      LOGGER.info("ORCID Bio Service Unsuccessfull! \nUrl:{} \nSTATUS:{} \nBODY:{}", url,
          response.getStatusCode(), response.getBody());
    }

    LOGGER.trace("Exiting getOrcIdBio:\n {}", result);
    return result;
  }

  
  
  @Override
  public String getOrcIdWorks(String orcId, Credential credential)
      throws ThorServiceException, NonAuthorizedThorException {
    LOGGER.trace("Starting getOrcIdWorks({},{})", orcId, credential);
    try {
      HttpRequestFactory requestFact = getHttpTransport().createRequestFactory(credential);
      GenericUrl url = new GenericUrl(String.format(orcidWorksUrl, orcId));
      HttpRequest request = requestFact.buildGetRequest(url);
      request.getHeaders().setAccept(MediaType.APPLICATION_JSON_VALUE);
      HttpResponse response = request.execute();
      //TODO catch the response code, if is not authorized (change the accesstoken to a invalid value before calling, to test the response code)
      //then should return a non authorized exception, so that we can logoff te user, clear the cookie, and make he logon again

      String jsonid = response.parseAsString();

      LOGGER.trace("Exiting getOrcIdWorks(): {}", jsonid);
      return jsonid;

    } catch (IOException e) {
      String errorMsg = "Error getting user works from OrcId Web Service";
      LOGGER.error(errorMsg, e);

      if (e instanceof HttpResponseException
          && ((HttpResponseException) e).getStatusCode() == 401) {
        throw new NonAuthorizedThorException(e);
      }
      
      throw new ThorServiceException(errorMsg, e);
    }
  }

  /**
   * @see OrcIdService#postOrcIdWork(String, Credential, OrcIdWork) .
   */
  public void postOrcIdWork(String orcId, Credential credential, OrcIdWork orcIdWork)
      throws ThorServiceException {
    LOGGER.trace("Starting postOrcIdWork({},{}, {})", orcId, credential, orcIdWork);
    try {
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
      //generate the REST based URL
      GenericUrl url = new GenericUrl(String.format(orcidWorksUrl, orcId));
      //generates the xml payload
      String requestBody = OrcIdXmlHelper.getAddWorksXml(orcIdWork);
      //creates the request
      HttpRequest request = requestFactory.buildPostRequest(url,
          ByteArrayContent.fromString(MediaType.APPLICATION_XML_VALUE, requestBody));
      request.getHeaders().setContentType(MediaType.APPLICATION_XML_VALUE);
      //Call the web service
      LOGGER.debug("posting Work...");
      HttpResponse response = request.execute();
      //Check if webservice ran successfully
      if (HttpStatus.OK.equals(response.getStatusCode())) {
        LOGGER.debug("ORCID Add Work Web Service successfull!");
        LOGGER.trace("Exiting postOrcIdWork()");
        return;
      } else {
        LOGGER.error(
            "ORCID Add Work Web Service Unsuccessfull! \nUrl:{} \nSTATUS:{} \n RESPONSE:{}",
            url,
            response.getStatusCode(), response);
      }

    } catch (Exception e) {
      LOGGER.error(e);
    }
    throw new ThorServiceException("Error adding Work to OrcId Web");
  }

  public RestTemplate getOrcidRestTemplate() {
    return orcidRestTemplate;
  }

  public void setOrcidRestTemplate(RestTemplate orcidRestTemplate) {
    this.orcidRestTemplate = orcidRestTemplate;
  }

  public String getOrcidSearchUrl() {
    return orcidSearchUrl;
  }

  public void setOrcidSearchUrl(String orcidSearchUrl) {
    this.orcidSearchUrl = orcidSearchUrl;
  }

  public String getOrcidBioUrl() {
    return orcidBioUrl;
  }

  public void setOrcidBioUrl(String orcidBioUrl) {
    this.orcidBioUrl = orcidBioUrl;
  }

  public String getOrcidInfoUrl() {
    return orcidInfoUrl;
  }

  public void setOrcidInfoUrl(String orcidInfoUrl) {
    this.orcidInfoUrl = orcidInfoUrl;
  }

  public String getOrcidWorksUrl() {
    return orcidWorksUrl;
  }

  public void setOrcidWorksUrl(String orcidWorksUrl) {
    this.orcidWorksUrl = orcidWorksUrl;
  }

  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  public void setJsonFactory(JsonFactory jsonFactory) {
    this.jsonFactory = jsonFactory;
  }

  public HttpTransport getHttpTransport() {
    return httpTransport;
  }

  public void setHttpTransport(HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
  }

}
