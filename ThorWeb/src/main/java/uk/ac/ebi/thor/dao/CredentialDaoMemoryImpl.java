package uk.ac.ebi.thor.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import uk.ac.ebi.literature.db.model.thor.ThorCredential;

/**
 * In memory implementation for CredentialDao.
 * @author Guilherme Formaggio
 */
@Component
public class CredentialDaoMemoryImpl implements CredentialDao {

  private static final Logger LOGGER = LogManager.getLogger(CredentialDaoMemoryImpl.class);

  Map<String, ThorCredential> map = new ConcurrentHashMap<>();

  @Override
  public void add(ThorCredential obj) {
    LOGGER.debug("Starting add()");
    if (obj != null && obj.getSessionId() != null) {
      map.putIfAbsent(obj.getSessionId(), obj);
    }
    LOGGER.debug("Exiting add()");
  }

  @Override
  public List<ThorCredential> findAll() {
    LOGGER.debug("findall()");
    return new ArrayList<>(map.values());
  }

  @Override
  public ThorCredential findById(String id) {
    LOGGER.debug("findById({})", id);
    return map.get(id);
  }

  @Override
  public void clear()
      throws IOException {
    LOGGER.debug("clear()");
    throw new IOException("Not allowed to clear all");

  }

  @Override
  public void remove(String key)
      throws IOException {
    LOGGER.debug("remove({})", key);
    map.remove(key);
  }

  @Override
  public void update(ThorCredential obj)
      throws IOException {
    if (obj != null) {
      remove(obj.getSessionId());
      add(obj);
    }
  }

  @Override
  public void removeByOrcId(String orcId)
      throws IOException {
    List<ThorCredential> allList = findAll();
    for (ThorCredential credential : allList) {
      if (orcId.equals(credential.getOrcId())) {
        map.remove(credential);
      }
    }
  }

}
