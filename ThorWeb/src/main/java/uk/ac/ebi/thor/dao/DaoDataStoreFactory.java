package uk.ac.ebi.thor.dao;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;

@Configurable
public class DaoDataStoreFactory extends AbstractDataStoreFactory {

  @Autowired
  private CredentialDao credentialDao;

  @SuppressWarnings("unchecked")
  @Override
  protected DataStore<StoredCredential> createDataStore(String id)
      throws IOException {
    DataStore<StoredCredential> ds = new DaoDataStore(this, id); 
    return ds;
  }

  public CredentialDao getCredentialDao() {
    return credentialDao;
  }

  public void setCredentialDao(CredentialDao credentialDao) {
    this.credentialDao = credentialDao;
  }

}
