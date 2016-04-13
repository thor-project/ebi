package uk.ac.ebi.thor.model;

import java.io.Serializable;

public class OrcIdWorkExtIdentifier implements Serializable {

  private static final long serialVersionUID = -7028950746234307846L;
  private String workExternalIdentifierType;
  private String workExternalIdentifierId;

  public String getWorkExternalIdentifierType() {
    return workExternalIdentifierType;
  }

  public void setWorkExternalIdentifierType(String workExternalIdentifierType) {
    this.workExternalIdentifierType = workExternalIdentifierType;
  }

  public String getWorkExternalIdentifierId() {
    return workExternalIdentifierId;
  }

  public void setWorkExternalIdentifierId(String workExternalIdentifierId) {
    this.workExternalIdentifierId = workExternalIdentifierId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((workExternalIdentifierId == null) ? 0 : workExternalIdentifierId.hashCode());
    result = prime * result
        + ((workExternalIdentifierType == null) ? 0 : workExternalIdentifierType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    OrcIdWorkExtIdentifier other = (OrcIdWorkExtIdentifier) obj;
    if (workExternalIdentifierId == null) {
      if (other.workExternalIdentifierId != null) {
        return false;
      }
    } else if (!workExternalIdentifierId.equals(other.workExternalIdentifierId)) {
      return false;
    }
    if (workExternalIdentifierType == null) {
      if (other.workExternalIdentifierType != null) {
        return false;
      }
    } else if (!workExternalIdentifierType.equals(other.workExternalIdentifierType)) {
      return false;
    }
    return true;
  }

}
