package uk.ac.ebi.thor.service;

import com.google.api.client.auth.oauth2.Credential;

import uk.ac.ebi.thor.exception.NonAuthorizedThorException;
import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdWork;

/**
 * Business service interface to get data from ORCID records.
 * @author Guilherme Formaggio
 */
public interface OrcIdService {

  /**
   * Proxy to search the ORCID registry for the ones matching the keyword.
   * @param keyword
   *        Keyword to search for ORCID records
   * @return JSON format with results keeping orcid response format
   */
  public String searchOrcIdsAllRecords(String keyword);

  /**
   * Return from OrcId Member webservice to search for works
   * @param codAuth
   *        Autorization Code for OAuth access member API on behalf of the current user.
   * @param loginRedirectUri
   *        Callback Url for OAuth Orcid loin
   * @return Json String from OrcId member api for works service.
   * @throws ThorServiceException
   *         Exception if error to access the web service results
   * @throws NonAuthorizedThorException
   *         If the accessToken is not accepted or user is not authorized.
   */
  public String getOrcIdWorks(String orcId, Credential credential)
      throws ThorServiceException, NonAuthorizedThorException;

  /**
   * Call the OrcID works web service to add a new Work to the user registry.
   * @param orcId
   *        User orcid
   * @param credential
   *        Credential with access token
   * @param orcIdWork
   *        Detials of work to be added
   * @throws ThorServiceException
   *         Exception if error to access the web service results
   */
  public void postOrcIdWork(String orcId, Credential credential, OrcIdWork orcIdWork)
      throws ThorServiceException;

  /**
   * Call the OrcID bio web services to retrive the user informations.
   * @param orcId Orc ID
   * @return JSON format with results keeping orcid response format
   */
  public String getOrcIdBio(String orcId);

}
