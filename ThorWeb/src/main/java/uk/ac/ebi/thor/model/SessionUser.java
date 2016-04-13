package uk.ac.ebi.thor.model;

import com.google.api.client.auth.oauth2.StoredCredential;

import java.io.Serializable;

public class SessionUser implements Serializable {

  private static final long serialVersionUID = 2551780956991577083L;

  //User Id for the current Thor request. 
  //Used as id to retrive his oauth credentials.
  private String sessionId;

  private OrcIdRecord orcIdRecord;

  //Use cookies to remember the user
  private Boolean remembeMe = Boolean.FALSE;
  
  private StoredCredential storedCredential;

  public SessionUser(String sessionId) {
    super();
    this.sessionId = sessionId;
  }

  /**
   * Constructor.
   * @param sessionId
   *        Unique id for the user.
   * @param remembeMe
   *        Remember of not the user after the browser is closed.
   */
  public SessionUser(String sessionId, Boolean remembeMe) {
    super();
    this.sessionId = sessionId;
    this.remembeMe = remembeMe;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Boolean getRemembeMe() {
    return remembeMe;
  }

  /**
   * Sets from string.
   * @param remembeMe "true" to Remember the user after the browser is closed.
   */
  public void setRemembeMe(String remembeMe) {
    if ("true".equalsIgnoreCase(remembeMe)) {
      setRemembeMe(Boolean.TRUE);
    }
  }

  public void setRemembeMe(Boolean remembeMe) {
    this.remembeMe = remembeMe;
  }

  public OrcIdRecord getOrcIdRecord() {
    return orcIdRecord;
  }

  public void setOrcIdRecord(OrcIdRecord orcIdRecord) {
    this.orcIdRecord = orcIdRecord;
  }

  public StoredCredential getStoredCredential() {
    return storedCredential;
  }

  public void setStoredCredential(StoredCredential storedCredential) {
    this.storedCredential = storedCredential;
  }

}
