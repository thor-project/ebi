package uk.ac.ebi.thor.model;

import java.io.Serializable;
import java.util.List;

/**
 * Class to represent the useful elements from one OrcId Record.
 * @author Guilherme Formaggio
 */
public class OrcIdRecord implements Serializable {

  private static final long serialVersionUID = 8752619395257094472L;

  private String orcId;
  private String name;
  private String givenName;
  private String familyName;
  private List<OrcIdWork> works;

  public OrcIdRecord() {
    super();
  }

  /**
   * Constructor.
   * @param orcId
   *        OrcID
   * @param name
   *        Name
   */
  public OrcIdRecord(String orcId) {
    super();
    this.orcId = orcId;
  }

  /**
   * Verify if the current OrcIdRecord contains a work with one external identifier equals to the
   * parameter.
   * @param orcIdWorkIdentifier
   *        OrcIdWorkExtIdentifier
   * @return True if any of the OrcId record works contain the informed identifier.
   */
  public Boolean hasWorkIdentifier(OrcIdWorkExtIdentifier orcIdWorkIdentifier) {
    if (orcIdWorkIdentifier != null && works != null) {
      //check all works...
      for (OrcIdWork myWorks : works) {
        if (myWorks != null && myWorks.getWorkExternalIdentifiers() != null) {
          //...and all external identifiers of each work
          for (OrcIdWorkExtIdentifier myWorkIdentifiers : myWorks.getWorkExternalIdentifiers()) {
            //...to see if they match
            if (orcIdWorkIdentifier.equals(myWorkIdentifiers)) {
              return Boolean.TRUE;
            }
          }
        }
      }
    }
    return Boolean.FALSE;
  }

  /**
   * Checks if string is empty or not.
   * @param str
   *        String
   * @return true if not empty or not null
   */
  private static boolean notEmpty(String str) {
    if (str != null && !"".equals(str.trim())) {
      return true;
    }
    return false;
  }

  public String getOrcId() {
    return orcId;
  }

  /**
   * Set orcid only if not empty.
   * @param orcId
   *        orcid.
   */
  public void setOrcId(String orcId) {
    if (notEmpty(orcId)) {
      this.orcId = orcId;
    }
  }

  /**
   * Returns the name, or givenName + fullName if name is empty. 
   * @return Name
   */
  public String getName() {
    if (notEmpty(name)) {
      return name;
    } else {
      StringBuilder sb = new StringBuilder();
      if (givenName != null) {
        sb.append(givenName).append(" ");
      }
      if (familyName != null) {
        sb.append(familyName);
      }
      return sb.toString().trim();
    }
  }

  /**
   * Set name only if not empty.
   * @param name
   *        name
   */
  public void setName(String name) {
    if (notEmpty(name)) {
      this.name = name;
    }
  }

  public List<OrcIdWork> getWorks() {
    return works;
  }

  public void setWorks(List<OrcIdWork> works) {
    this.works = works;
  }

  public String getGivenName() {
    return givenName;
  }

  /**
   * Set given name only if not empty.
   * @param givenName
   *        Name
   */
  public void setGivenName(String givenName) {
    if (notEmpty(givenName)) {
      this.givenName = givenName;
    }
  }

  public String getFamilyName() {
    return familyName;
  }

  /**
   * Set family name only if not empty.
   * @param familyName
   *        family name
   */
  public void setFamilyName(String familyName) {
    if (notEmpty(familyName)) {
      this.familyName = familyName;
    }
  }

}
