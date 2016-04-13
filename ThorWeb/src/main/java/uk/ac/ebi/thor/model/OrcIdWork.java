package uk.ac.ebi.thor.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent part of data from one 'orcid-work' element from the orcid record.
 * @author Guilherme Formaggio
 */
public class OrcIdWork implements Serializable {

  private static final long serialVersionUID = 6835683212785290821L;
  private String title;
  private String subtitle;
  private String workType;
  private String publicationYear;
  private String url;
  private List<OrcIdWorkExtIdentifier> workExternalIdentifiers;

  public OrcIdWork() {
  }

  /**
   * Constructor from parameters.
   * @param title
   *        String
   * @param subtitle
   *        String
   * @param workType
   *        String
   * @param publicationYear
   *        String
   * @param workExternalIdentifierType
   *        String
   * @param workExternalIdentifierId
   *        String
   */
  public OrcIdWork(String title, String subtitle, String workType, String publicationYear,
      String workExternalIdentifierType, String workExternalIdentifierId, String url) {
    super();
    this.title = title;
    this.subtitle = subtitle;
    this.workType = workType;
    this.publicationYear = publicationYear;
    this.url = url;
    OrcIdWorkExtIdentifier work = new OrcIdWorkExtIdentifier();
    work.setWorkExternalIdentifierId(workExternalIdentifierId);
    work.setWorkExternalIdentifierType(workExternalIdentifierType);
    this.workExternalIdentifiers = Arrays.asList(work);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public String getWorkType() {
    return workType;
  }

  public void setWorkType(String workType) {
    this.workType = workType;
  }

  public List<OrcIdWorkExtIdentifier> getWorkExternalIdentifiers() {
    return workExternalIdentifiers;
  }

  public void setWorkExternalIdentifiers(List<OrcIdWorkExtIdentifier> workExternalIdentifiers) {
    this.workExternalIdentifiers = workExternalIdentifiers;
  }

  public String getPublicationYear() {
    return publicationYear;
  }

  public void setPublicationYear(String publicationYear) {
    this.publicationYear = publicationYear;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
