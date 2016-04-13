package uk.ac.ebi.thor.dao;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.ebi.literature.db.dao.ICrudDAO;
import uk.ac.ebi.literature.db.model.thor.ThorCredential;

public class CredentialDaoJpaImpl implements CredentialDao {

  private static final Logger LOGGER = LogManager.getLogger(CredentialDaoJpaImpl.class);

  private ICrudDAO crudDao;

  @Override
  public void add(ThorCredential obj)
      throws IOException {
    LOGGER.trace("Starting add()");
    boolean success = crudDao.insertEntry(obj);
    if (success) {
      LOGGER.info("Credential added to database with success!");
    } else {
      IOException ex = new IOException("Error inserting Credential to Database");
      LOGGER.error(ex);
      throw ex;
    }
    LOGGER.trace("Exiting add()");
  }

  @Override
  public List<ThorCredential> findAll() {
    LOGGER.trace("Starting findAll()");
    List<ThorCredential> result = crudDao.readAllElements(ThorCredential.class);
    LOGGER.trace("Exiging findAll():{}", result != null ? result.size() : "null");
    return result;
  }

  @Override
  public ThorCredential findById(String id) {
    LOGGER.trace("Starting findById({})", id);
    ThorCredential tc = null;
    if (id != null && !"".equals(id)) {
      tc = crudDao.readById(ThorCredential.class, id);
    }
    LOGGER.trace("Exiting findById():", tc != null ? "found" : "not found");
    return tc;

  }

  /**
   * Set all credentials in the database to active = false.
   * @throws IOException
   *         Exception in case of database error.
   */
  @Override
  public void clear()
      throws IOException {
    IOException ex = new IOException("Not allowed to delete all credentials.");
    LOGGER.error(ex);
    throw ex;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void removeByOrcId(String orcId)
      throws IOException {
    LOGGER.trace("Starting removeByOrcId({})", orcId);
    String query = "FROM ThorCredential WHERE orcId = ?";
    List<ThorCredential> tcList = crudDao.readAllByUserQuery(query, orcId);
    if (tcList != null && !tcList.isEmpty()) {
      boolean success = crudDao.deleteEntries(tcList);
      if (success) {
        LOGGER.info("Old credentials for OrcId {} removed from database with success!", orcId);
      } else {
        IOException ex = new IOException(
            String.format("Error removing old credentials for OrcId %s from Database", orcId));
        LOGGER.error(ex);
        throw ex;
      }
    } else {
      LOGGER.info("No credentials for OrcId {} exists in database.", orcId);
    }
    LOGGER.trace("Exiting removeByOrcId()");
  }

  @Override
  public void remove(String key)
      throws IOException {
    LOGGER.trace("Starting remove({})", key);
    ThorCredential tc = findById(key);
    boolean success = crudDao.deleteEntry(tc);
    if (success) {
      LOGGER.info("Credential {} removed from database with success!", key);
    } else {
      IOException ex = new IOException(
          String.format("Error removing Credential %s from Database", key));
      LOGGER.error(ex);
      throw ex;
    }
    LOGGER.trace("Exiting remove()");
  }

  public ICrudDAO getCrudDao() {
    return crudDao;
  }

  public void setCrudDao(ICrudDAO crudDao) {
    this.crudDao = crudDao;
  }

  @Override
  public void update(ThorCredential obj)
      throws IOException {
    LOGGER.trace("Starting update()");
    boolean success = crudDao.updateEntry(obj);
    if (success) {
      LOGGER.info("Credential Updated in database with success!");
    } else {
      IOException ex = new IOException("Error updating Credential to Database");
      LOGGER.error(ex);
      throw ex;
    }
    LOGGER.trace("Exiting update()");
  }

}
