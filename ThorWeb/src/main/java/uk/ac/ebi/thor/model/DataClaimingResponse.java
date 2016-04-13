package uk.ac.ebi.thor.model;

import java.io.Serializable;

public class DataClaimingResponse implements Serializable {

  private static final long serialVersionUID = -6635718540722183171L;

  private Boolean isUserLoggedIn = Boolean.FALSE;
  private String loginUrl;
  private String logoutUrl;
  private OrcIdRecord orcIdRecord;
  private Boolean isDataClaimed = Boolean.FALSE;

  public Boolean getIsUserLoggedIn() {
    return isUserLoggedIn;
  }

  public void setIsUserLoggedIn(Boolean isUserLoggedIn) {
    this.isUserLoggedIn = isUserLoggedIn;
  }

  public String getLoginUrl() {
    return loginUrl;
  }

  public void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
  }

  public OrcIdRecord getOrcIdRecord() {
    return orcIdRecord;
  }

  public void setOrcIdRecord(OrcIdRecord orcIdRecord) {
    this.orcIdRecord = orcIdRecord;
  }

  public String getLogoutUrl() {
    return logoutUrl;
  }

  public void setLogoutUrl(String logoutUrl) {
    this.logoutUrl = logoutUrl;
  }

  public Boolean getIsDataClaimed() {
    return isDataClaimed;
  }

  public void setIsDataClaimed(Boolean isDataClaimed) {
    this.isDataClaimed = isDataClaimed;
  }

}
