package uk.ac.ebi.thor.exception;

public class NonAuthorizedThorException extends Exception {

  private static final long serialVersionUID = 2918812730008481741L;

  public NonAuthorizedThorException(Exception ex) {
    super(ex);
  }
}
