package uk.ac.ebi.thor.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;

import uk.ac.ebi.literature.db.model.thor.ThorCredential;

public class DaoDataStore extends AbstractDataStore<StoredCredential> {

  private static Logger LOGGER = LogManager.getLogger(DaoDataStore.class);

  protected DaoDataStore(DaoDataStoreFactory dataStoreFactory, String id) {
    super(dataStoreFactory, id);
  }

  private CredentialDao getDao() {
    return ((DaoDataStoreFactory) getDataStoreFactory()).getCredentialDao();
  }

  @Override
  public Set<String> keySet()
      throws IOException {
    LOGGER.debug("Starting keySet()");
    Set<String> result = new HashSet<>();
    List<ThorCredential> list = getDao().findAll();

    if (list != null && !list.isEmpty()) {
      for (ThorCredential credential : list) {
        result.add(credential.getSessionId());
      }
    }
    LOGGER.debug("Exiting keySet():{}", ((result != null) ? result.size() : "null"));
    return result;
  }

  @Override
  public Collection<StoredCredential> values()
      throws IOException {
    LOGGER.debug("Starting values()");
    List<StoredCredential> result = new ArrayList<>();
    List<ThorCredential> list = getDao().findAll();

    if (list != null && !list.isEmpty()) {
      for (ThorCredential credential : list) {
        StoredCredential sc = CredentialHelper.populateStoredCredential(credential);
        result.add(sc);
      }
    }
    LOGGER.debug("Exiting values():{}", ((result != null) ? result.size() : "null"));
    return result;
  }

  @Override
  public StoredCredential get(String key)
      throws IOException {
    LOGGER.debug("Starting get({})", key);
    ThorCredential credential = getDao().findById(key);
    StoredCredential sc = CredentialHelper.populateStoredCredential(credential);
    LOGGER.debug("Exiting get()");
    return sc;
  }


  @Override
  public DataStore<StoredCredential> set(String key, StoredCredential value)
      throws IOException {
    LOGGER.debug("Starting set({})", key);
    ThorCredential thorCredential = getDao().findById(key);
    if (thorCredential == null) {
      
      thorCredential = new ThorCredential();
      thorCredential.setAccessToken(value.getAccessToken());
      thorCredential.setCreationDate(new Date());
      thorCredential.setExpirationTimeMilliseconds(value.getExpirationTimeMilliseconds());
      thorCredential.setRefreshToken(value.getRefreshToken());
      thorCredential.setSessionId(key);
      
      try {
        getDao().add(thorCredential);
        LOGGER.debug("Credential for SessionUserId={} inserted to database with success!", key);
      } catch (IOException e) {
        LOGGER.error("Error inserting Credential to Database for SessionUserId:{}", key);
        throw e;
      }
    }
    LOGGER.debug("Exiting set()");
    return this;
  }

  @Override
  public DataStore<StoredCredential> clear()
      throws IOException {
    LOGGER.debug("Starting clear()");
    getDao().clear();
    LOGGER.debug("Exiting clear()");
    return this;
  }

  @Override
  public DataStore<StoredCredential> delete(String key)
      throws IOException {
    LOGGER.debug("Starting delete({})", key);
    getDao().remove(key);
    LOGGER.debug("Exiting delete()");
    return this;
  }

}
