package uk.ac.ebi.thor.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.auth.oauth2.StoredCredential;

import uk.ac.ebi.literature.db.model.thor.ThorCredential;

public class CredentialHelper {
  
  private static Logger LOGGER = LogManager.getLogger(CredentialHelper.class);

  private CredentialHelper() {
    
  }
  
  /**
   * Creates a Stored Credential from a Thor Credential.
   * @param credential
   *        Thor Credential
   * @return Null if ThorCredential is null
   */
  public static StoredCredential populateStoredCredential(ThorCredential credential) {
    LOGGER.trace("Starting populateStoredCredential()");
    StoredCredential sc = null;
    if (credential != null) {
      sc = new StoredCredential();
      sc.setAccessToken(credential.getAccessToken());
      sc.setExpirationTimeMilliseconds(credential.getExpirationTimeMilliseconds());
      sc.setRefreshToken(credential.getRefreshToken());
    }
    LOGGER.trace("Exiting populateStoredCredential()");
    return sc;
  }

}
