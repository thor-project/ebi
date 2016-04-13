package uk.ac.ebi.thor.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;

import uk.ac.ebi.thor.exception.NonAuthorizedThorException;
import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdRecord;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.SessionUser;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Interface that provides Http Request/Response services to manage the user authentication.
 * @author Guilherme Formaggio
 */
public interface DataClaimingService {

  public static final String URL_LOGIN = "/api/dataclaiming/login";
  public static final String URL_CALLBACKLOGIN = "/api/dataclaiming/logincallback";
  public static final String URL_LOGOUT = "/api/dataclaiming/logout";
  
  public static final String COOKIE_NAME = "ThorOrcIdUser";
  public static final String USERID_SESSION = "THORORCIDUSERID";
  public static final String PARAM_REMEMBER_ME = "remind";
  public static final String PARAM_CLIENTADD = "clientAddress";

  public String getCallBackUrl(HttpServletRequest request);

  public String getLoginUrl(HttpServletRequest request);

  public String getLogoutUrl(HttpServletRequest request);

//  /**
//   * After successful authentication, makes the user logged in the application.
//   * @param req
//   *        HttpServletRequest
//   * @param resp
//   *        HttpServletResponse
//   * @param credential
//   *        Credential
//   */
//  public void
//      registerUserLogin(HttpServletRequest req, HttpServletResponse resp, Credential credential);

  /**
   * Makes the user logged out the application and delete "remember-me" cookie.
   * @param req
   *        HttpServletRequest
   * @param resp
   *        HttpServletResponse
   * @param credential
   *        Credential
   */
  public void
      registerUserLogout(HttpServletRequest req, HttpServletResponse resp);

  /**
   * Creates if necessary and returns the current user from session, with the unique identification
   * and remember me parameters filled.
   * @param request
   *        HttpServletRequest for the user
   * @return SessionUser
   */
  public SessionUser retrieveUserSession(HttpServletRequest request);

  /**
   * Creates an authorization flow for google Oauth.
   * @param addCredentialCreateListener
   *        true to add a Credential Create Listener to the flow.
   * @return AuthorizationCodeFlow
   * @throws IOException
   *         exception
   */
  public AuthorizationCodeFlow createAuthorizationFlow(boolean addCredentialCreateListener)
      throws IOException;

  /**
   * Verify if the user have signed within Orc Id.
   * @param sessionUser
   *        SessionUser of current user
   * @return true if user exists in http session
   */
  public Boolean isUserLoggedIn(SessionUser sessionUser);

  /**
   * Fetches all Works from OrcId and retrieve the OrcId record. The Uses must already be logged in
   * the application
   * @param request
   *        HttpServletRequest
   * @return OrcIdRecord loaded with works from OrcId
   * @throws ThorServiceException
   *         exception
   * @throws NonAuthorizedThorException
   *         IF user credentials are not accepted by OrcId, so the user must be logged out to try to
   *         login again
   */
  public OrcIdRecord loadOrcIdRecord(HttpServletRequest request)
      throws ThorServiceException, NonAuthorizedThorException;

  /**
   * Add a Work to the OrcId record.
   * @param request
   *        HttpServletRequest with userdetails
   * @param orcIdWork
   *        Work to be added to the user record
   * @throws ThorServiceException
   *         If errors
   */
  public void claimOrcIdWork(HttpServletRequest request, OrcIdWork orcIdWork)
      throws ThorServiceException;

  /**
   * Creates a user into the session, with the unique identification.
   * @param session
   *        HttpSession for the request
   * @return SessionUser
   */
  public SessionUser newUserSession(HttpSession session);

  /**
   * Manages the cookie to remember the user or not. If the user wants to be remembered, add the
   * cookie; if not, removes it.
   * @param req
   *        Must contain the SessionUser with the parameter set to remember the user or not
   * @param resp
   *        HttpServletResponse with added cookie information.
   */
  public void registerCookie(HttpServletRequest req, HttpServletResponse resp);

  /**
   * Adds the OrcId number and Credential to the Session User.
   * @param req
   *        HttpServletRequest
   * @param credential
   *        Credential
   */
  public void registerOrcIdDetails(HttpServletRequest req, Credential credential);

  /**
   * Set te remember me parameter in the user session.
   * @param req HttpServletRequest
   */
  public void registerRememberMe(HttpServletRequest req);

  /**
   * @see OrcIdService#getOrcIdBio(String) .
   */
  public String getOrcIdBio(String orcId);

  /**
   * Registrer the user server:port that the client application is running, in case the browser is
   * not sending the 'origin cookie'.
   * @param req
   *        HttpServletRequest
   */
  public void registerClientAddress(HttpServletRequest req);

}
