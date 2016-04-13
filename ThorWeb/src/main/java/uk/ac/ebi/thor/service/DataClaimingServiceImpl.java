package uk.ac.ebi.thor.service;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow.CredentialCreatedListener;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.literature.db.model.thor.ThorCredential;
import uk.ac.ebi.thor.dao.CredentialDao;
import uk.ac.ebi.thor.dao.CredentialHelper;
import uk.ac.ebi.thor.exception.NonAuthorizedThorException;
import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdRecord;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.SessionUser;
import uk.ac.ebi.thor.model.helper.OrcIdJsonHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Class for Http Request/Cookies management and to implement user authentication at OrcId based on
 * Google OAuth.
 * @author Guilherme Formaggio
 */
public class DataClaimingServiceImpl implements DataClaimingService {

  private static final Logger LOGGER = LogManager.getLogger(DataClaimingServiceImpl.class);

  private static String LOGIN_URL;
  private static String LOGOUT_URL;
  private static String CALLBACK_URL;

  private String serverName; //Thor server name:port for url redirection
  private String accessTokenUri;
  private JsonFactory jsonFactory;
  private HttpTransport httpTransport;

  //Client App id
  private String clientId;
  private String clientSecret;

  private String userAuthorizationUri;
  private Collection<String> scopes;

  private CredentialListener credentialListener = new CredentialListener();

  @Autowired
  private OrcIdService service;

  @Autowired
  DataStoreFactory dataStoreFactory;
  @Autowired
  CredentialDao credentialDao;

//  /**
//   * @see DataClaimingService#registerUserLogin (HttpServletRequest, HttpServletResponse,
//   *      Credential).
//   */
//  @Override
//  public void
//      registerUserLogin(HttpServletRequest req, HttpServletResponse resp, Credential credential) {
//    LOGGER.debug("Entering registerUserLogin()");
//    registerOrcIdDetails(req, credential);
//    registerCookie(req, resp);
//    LOGGER.debug("Exiting registerUserLogin()");
//  }

  /**
   * @see DataClaimingService#registerUserLogout(HttpServletRequest, HttpServletResponse,
   *      Credential) .
   */
  @Override
  public void
      registerUserLogout(HttpServletRequest req, HttpServletResponse resp) {
    LOGGER.trace("Entering registerUserLogout()");
    registerCookie(req, resp);
//    try {
//      //Remove access token from database
//      credentialDao.remove(retrieveUserSession(req).getSessionId());
//    } catch (IOException e) {
//      LOGGER.error("AccessToken not found in database during logout!");
//    }

    //Remove user from session
    req.getSession().removeAttribute(USERID_SESSION);
    req.getSession().invalidate();
    LOGGER.trace("Exiting registerUserLogout()");
  }

  /**
   * @see DataClaimingService#registerOrcIdDetails(HttpServletRequest, Credential) .
   */
  @Override
  public void registerOrcIdDetails(HttpServletRequest req, Credential credential) {
    LOGGER.trace("Entering  registerOrcId()");
    String accessToken = credential.getAccessToken();
    String[] tokenInfo = credentialListener.pullTokenInfo(accessToken);
    String orcId = tokenInfo[0];
    String scope = tokenInfo[1];
    SessionUser sessionUser = retrieveUserSession(req);
    sessionUser.setOrcIdRecord(new OrcIdRecord(orcId));
    sessionUser.setStoredCredential(new StoredCredential(credential));
    try {
      //Adds OrcId value to the credential in the database
      updateCredentialOrcId(sessionUser.getSessionId(), orcId, scope,
          credential.getTokenServerEncodedUrl());
    } catch (IOException ex) {
      LOGGER.error("Could not update OrcId for the credential in database!");
    }
    LOGGER.trace("Exiting registerOrcId()");
  }

  /**
   * Updates the thor credential in the database, adding the OrcId value.
   * @param sessionId
   *        Key for the user credential
   * @param orcId
   *        OrcId for the user
   * @throws IOException
   *         In case of database error
   */
  private void
      updateCredentialOrcId(String sessionId, String orcId, String scope, String tokenServerUrl)
          throws IOException {
    //Update current accessToken infomation with OrcId number
    ThorCredential tc = credentialDao.findById(sessionId);
    tc.setOrcId(orcId);
    tc.setScope(scope);
    tc.setTokenServerUrl(tokenServerUrl);
    credentialDao.update(tc);
  }

  /**
   * @see DataClaimingService#registerRememberMe(HttpServletRequest) .
   */
  @Override
  public void registerRememberMe(HttpServletRequest req) {
    String rememberMe = req.getParameter(PARAM_REMEMBER_ME);
    LOGGER.debug("Parameter 'Remember me': {}", rememberMe);
    SessionUser sessionUser = retrieveUserSession(req);
    sessionUser.setRemembeMe(rememberMe);
  }

  /**
   * @see DataClaimingService#registerClientAddress(HttpServletRequest) .
   */
  @Override
  public void registerClientAddress(HttpServletRequest req) {
    String clientAddress = req.getParameter(PARAM_CLIENTADD);
    LOGGER.debug("Parameter ClientAddress: {}", clientAddress);
    HttpSession session = req.getSession();
    if (session.getAttribute(DataClaimingService.PARAM_CLIENTADD) == null) {
      session.setAttribute(DataClaimingService.PARAM_CLIENTADD, clientAddress);
    }
  }

  /**
   * @see DataClaimingService#registerCookie(HttpServletRequest, HttpServletResponse) .
   */
  @Override
  public void registerCookie(HttpServletRequest req, HttpServletResponse resp) {
    LOGGER.trace("Entering  registerCookie()");
    SessionUser sessionUser = retrieveUserSession(req);
    Cookie cookie = retrieveUserIdCookie(req);
    //Check if user wants to be remembered next time that access the application...
    if (isUserLoggedIn(sessionUser) && sessionUser.getRemembeMe()) {
      // if (cookie == null) {
      //creates a cookie to remember the user
      cookie = new Cookie(COOKIE_NAME, retrieveUserSession(req).getSessionId());
      cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
      resp.addCookie(cookie);
      LOGGER.debug("Cookie added to HttpResponse.");
      //}
    } else { //The user doesnt want to be remembered 
      if (cookie != null) { //Removes the cookie if it exists.
        cookie.setValue(null);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        LOGGER.debug("Cookie removed from HttpResponse.");
      }
    }
    LOGGER.trace("Exiting  registerCookie()");
  }

  /**
   * @see DataClaimingService#claimOrcIdWork(HttpServletRequest, OrcIdWork) .
   */
  public void claimOrcIdWork(HttpServletRequest request, OrcIdWork orcIdWork)
      throws ThorServiceException {
    LOGGER.trace("Entering claimOrcIdWork()");
    SessionUser sessionUser = retrieveUserSession(request);

    //If user is not logged in OrcId, we cannot claim his work
    if (!isUserLoggedIn(sessionUser)) {
      String msg = "The User is not Logged in!";
      LOGGER.error(msg);
      throw new ThorServiceException(msg);
    }

    //Get user informations to claim a work into his orcid record
    String orcId = sessionUser.getOrcIdRecord().getOrcId();
    Credential credential = buildCredential(sessionUser.getStoredCredential());

    //claim a work to his orcid
    try {
      service.postOrcIdWork(orcId, credential, orcIdWork);
    } catch (ThorServiceException e) {
      LOGGER.error(String.format("Error adding Work to OrcId=%s", orcId), e);
      throw e;
    }
    LOGGER.trace("Exiting claimOrcIdWork()");
  }

  /**
   * @see DataClaimingService#isUserLoggedIn(HttpSession) .
   */
  @Override
  public Boolean isUserLoggedIn(SessionUser sessionUser) {
    LOGGER.trace("Entering isUserLoggedIn()");
    //The user is considered signed in if we know his orcid.
    if (sessionUser != null && sessionUser.getOrcIdRecord() != null
        && sessionUser.getOrcIdRecord().getOrcId() != null) {
      LOGGER.trace("Exiting isUserLoggedIn() : TRUE");
      return Boolean.TRUE;
    }
    LOGGER.trace("Exiting isUserLoggedIn() : FALSE");
    return Boolean.FALSE;
  }

  /**
   * @see DataClaimingService#getOrcIdBio(String).
   */
  @Override
  public String getOrcIdBio(String orcId) {
    LOGGER.trace("Entering getOrcIdBio()");
    return service.getOrcIdBio(orcId);
  }

  /**
   * @see DataClaimingService#loadOrcIdRecord(HttpServletRequest) .
   */
  @Override
  public OrcIdRecord loadOrcIdRecord(HttpServletRequest request)
      throws ThorServiceException, NonAuthorizedThorException {
    LOGGER.trace("Entering loadOrcIdRecord()");
    OrcIdRecord record = null;
    SessionUser sessionUser = retrieveUserSession(request);
    if (isUserLoggedIn(sessionUser)) {
      record = sessionUser.getOrcIdRecord();
      try {
        String orcId = record.getOrcId();
        //Retrieve user name
        String jsonBio = service.getOrcIdBio(orcId);
        OrcIdJsonHelper.populateOrcIdRecord(record, jsonBio);
        //Retrieve works
        String json = service.getOrcIdWorks(orcId,
            buildCredential(sessionUser.getStoredCredential()));
        OrcIdJsonHelper.populateOrcIdRecord(record, json);

        LOGGER.debug("OrcId {} was populated with works: {}", orcId, json);
      } catch (ThorServiceException e) {
        LOGGER.error(e);
        throw e;
      } catch (NonAuthorizedThorException e) {
        //User credentials were not accepted by orcid,
        //so the user must be logged out to try to login again
        LOGGER.error(e);
        throw e;
      }
    }
    LOGGER.trace("Exiting loadOrcIdRecord()");
    return record;
  }

  @Override
  public String getLogoutUrl(HttpServletRequest request) {
    if (LOGOUT_URL == null) {
      LOGOUT_URL = createUrl(request.getContextPath(), URL_LOGOUT, null);
    }
    return LOGOUT_URL;
  }

  @Override
  public String getLoginUrl(HttpServletRequest request) {
    if (LOGIN_URL == null) {
      LOGIN_URL = createUrl(request.getContextPath(), URL_LOGIN, null);
    }
    return LOGIN_URL;
  }

  @Override
  public String getCallBackUrl(HttpServletRequest request) {
    if (CALLBACK_URL == null) {
      CALLBACK_URL = createUrl(request.getContextPath(), URL_CALLBACKLOGIN, null);
    }
    return CALLBACK_URL;
  }

  /**
   * Creates a url string for the server:port configured at the "serverName" variable.
   * @param appContext
   *        Application context
   * @param path
   *        Url Path
   * @return Complete Url
   */
  private String createUrl(String appContext, String path, Map<String, Object> pathParams) {
    LOGGER.trace("Entering createUrl({},{})", appContext, path);
    GenericUrl url = new GenericUrl(serverName);
    url.setRawPath(appContext + path);
    if (pathParams != null) {
      url.setUnknownKeys(pathParams);
    }
    String result = url.build();
    LOGGER.trace("Exiting createUrl(): {}", result);
    return result;
  }

  /**
   * Retrive the cookie that stores the user id.
   * @param request
   *        HttpServletRequest
   * @return Unique user id or null if not found in the cookies.
   */
  private Cookie retrieveUserIdCookie(HttpServletRequest request) {
    LOGGER.trace("Entering retrieveUserIdCookie()");
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        Cookie cookie = cookies[i];
        if (COOKIE_NAME.equals(cookie.getName())) {
          LOGGER.debug("Exiting retrieveUserIdCookie(): {}", cookie.toString());
          return cookie;
        }
      }
    }
    LOGGER.trace("Exiting retrieveUserIdCookie(): null");
    return null;
  }

  /**
   * @see DataClaimingService#retrieveUserId(HttpServletRequest) .
   */
  @Override
  public SessionUser retrieveUserSession(HttpServletRequest request) {
    LOGGER.trace("Entering retrieveUserSession()");
    HttpSession session = request.getSession();

    //Checks if is user already in http session...
    if (session.getAttribute(USERID_SESSION) != null) {
      LOGGER.trace("user ID found in Http Session");
      return (SessionUser) session.getAttribute(USERID_SESSION);
    } else {
      SessionUser sessionUser;
      Cookie cookie = retrieveUserIdCookie(request);
      //checks if is returning user with id saved in cookie...
      if (cookie != null) {
        sessionUser = loadUserSession(cookie, session);
      } else {
        //If user not in session or cookie, creates a new unique user id
        sessionUser = newUserSession(session);
      }

      return sessionUser;
    }
  }

  /**
   * Load user information from cookie.
   * @param cookie
   *        Cookie
   * @param session
   *        HttpSession
   * @return SessionUser
   */
  private SessionUser loadUserSession(Cookie cookie, HttpSession session) {
    String userId = cookie.getValue();
    LOGGER.trace("user ID {} found in Cookie", userId);
    SessionUser sessionUser = new SessionUser(userId);
    ThorCredential tc = credentialDao.findById(userId);
    if (tc != null && tc.getOrcId() != null && !"".equals(tc.getOrcId().trim())) {
      StoredCredential sc = CredentialHelper.populateStoredCredential(tc);
      sessionUser.setStoredCredential(sc);
      OrcIdRecord record = new OrcIdRecord(tc.getOrcId());
      sessionUser.setOrcIdRecord(record);
    } else {
      //If the orcId for the User is not in the database,
      //the user is considered not logged in the application
      LOGGER.info("No OrcId found in the database for userId from the cookie");
    }
//Add user info to http session
    session.setAttribute(USERID_SESSION, sessionUser);
    LOGGER.trace("Exiting retrieveUserSession()");
    return sessionUser;
  }

  /**
   * @see DataClaimingService#newUserSession(HttpSession) .
   */
  @Override
  public SessionUser newUserSession(HttpSession session) {
    String userId = null;
    if (userId == null) {
      userId = UUID.randomUUID().toString();
      LOGGER.trace("user ID {} created", userId);
    }
    SessionUser sessionUser = new SessionUser(userId);

    //Add user info to http session
    session.setAttribute(USERID_SESSION, sessionUser);

    LOGGER.trace("Exiting retrieveUserSession()");
    return sessionUser;
  }

  /**
   * @see DataClaimingService#createAuthorizationFlow() .
   */
  @Override
  public AuthorizationCodeFlow createAuthorizationFlow(boolean addCredentialCreateListener)
      throws IOException {
    LOGGER.trace("Entering createAuthorizationFlow({})", addCredentialCreateListener);
    AuthorizationCodeFlow.Builder flow = new AuthorizationCodeFlow.Builder(
        BearerToken.authorizationHeaderAccessMethod(),
        httpTransport,
        jsonFactory, new GenericUrl(accessTokenUri),
        new BasicAuthentication(clientId, clientSecret), clientId,
        userAuthorizationUri)
            .setScopes(scopes)
            .setCredentialDataStore(
                StoredCredential.getDefaultDataStore(dataStoreFactory));

    //Adds a Listener to be invoked every time a new credential 
    //is created (every time a user makes login at OrcId OAuth)
    if (addCredentialCreateListener) {
      LOGGER.debug("adding CredentialCreateListener");
      flow.setCredentialCreatedListener(credentialListener);
    }

    LOGGER.trace("Exiting createAuthorizationFlow()");
    return flow.build();
  }

  /**
   * Converts an storedCredential into a Credential.
   * @param storedCredential
   *        must contain accesstoken
   * @return Credential
   */
  public Credential buildCredential(StoredCredential storedCredential) {
    Credential credential = new GoogleCredential.Builder()
        .setClientSecrets(clientId, clientSecret)
        .setTransport(httpTransport)
        .setJsonFactory(jsonFactory).build();

    credential.setRefreshToken(storedCredential.getRefreshToken());
    credential.setAccessToken(storedCredential.getAccessToken());
    credential.setExpirationTimeMilliseconds(storedCredential.getExpirationTimeMilliseconds());

    return credential;
  }

  /**
   * Convert the scopes string (separated by ';') into a collection of strings.
   * @param oauthScopes
   *        String of scopes separated by ';'
   */
  public void setScopes(String oauthScopes) {
    if (oauthScopes != null) {
      this.scopes = Arrays.asList(oauthScopes.split(";"));
    } else {
      this.scopes = null;
    }
  }

  public String getAccessTokenUri() {
    return accessTokenUri;
  }

  public void setAccessTokenUri(String accessTokenUri) {
    this.accessTokenUri = accessTokenUri;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getUserAuthorizationUri() {
    return userAuthorizationUri;
  }

  public void setUserAuthorizationUri(String userAuthorizationUri) {
    this.userAuthorizationUri = userAuthorizationUri;
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
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

  /**
   * CredentialCreatedListener invoked every time a user logs into OrcId OAuth. This class manages a
   * map to register the returned OrcId from the authentication service to the AccessToken stored in
   * the user credential.
   * @author Guilherme Formaggio
   */
  private class CredentialListener implements CredentialCreatedListener {

    private final Logger logger = LogManager.getLogger(CredentialListener.class);

    /**
     * Map to store the OrcId/Scope returned in the authentication header for the accesstoken.
     */
    private Map<String, String[]> orcIdsMap = new HashMap<>();

    @Override
    public void onCredentialCreated(Credential credential, TokenResponse tokenResponse)
        throws IOException {
      logger.trace("Entering onCredentialCreated()");
      String orcId = (String) tokenResponse.getUnknownKeys().get("orcid");
      String scope = (String) tokenResponse.getScope();
      String accessToken = credential.getAccessToken();
      orcIdsMap.putIfAbsent(accessToken, new String[] { orcId, scope });
      logger.debug("AccessToken {} Mapped to OrcID {}. \nExiting onCredentialCreated()",
          accessToken, orcId);
    }

    /**
     * Returns the OrcId related to the accessToken. The mapping is discarded after the return.
     * @param accessToken
     *        User access token for authentication
     * @return String OrcId of the user
     */
    public String[] pullTokenInfo(String accessToken) {
      logger.trace("Entering pullTokenInfo({})", accessToken);
      String[] tokenInfo = orcIdsMap.get(accessToken);
      if (tokenInfo != null) {
        logger.debug("OrcID {} mapping removed for AccessToken {} on scope {}", tokenInfo[0],
            accessToken, tokenInfo[1]);
        orcIdsMap.remove(accessToken);
      }
      logger.trace("Exiting pullTokenInfo()");
      return tokenInfo;
    }

  }

}
