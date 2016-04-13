package uk.ac.ebi.thor.exception;

public class ThorServiceException extends Exception {

  private static final long serialVersionUID = 1L;

  public ThorServiceException(String message) {
    super(message);
  }
  
  public ThorServiceException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
