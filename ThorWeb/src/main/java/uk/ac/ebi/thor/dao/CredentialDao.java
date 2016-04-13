package uk.ac.ebi.thor.dao;

import uk.ac.ebi.literature.db.model.thor.ThorCredential;

import java.io.IOException;
import java.util.List;


public interface CredentialDao {

  public void add(ThorCredential obj) throws IOException;
  
  public void update(ThorCredential obj) throws IOException;
  
  public List<ThorCredential> findAll();

  public ThorCredential findById(String id);

  public void clear()
          throws IOException;

  public void remove(String key)
          throws IOException;

  public void removeByOrcId(String orcId)
          throws IOException;

}
