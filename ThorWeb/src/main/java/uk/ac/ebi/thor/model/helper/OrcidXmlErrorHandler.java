package uk.ac.ebi.thor.model.helper;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OrcidXmlErrorHandler implements ErrorHandler {

  private List<String> warnings;
  private List<String> errors;
  private List<String> fatalErrors;

  private boolean success = true;

  public OrcidXmlErrorHandler() {
    setSuccess(true);
    warnings = new ArrayList<String>();
    errors = new ArrayList<String>();
    fatalErrors = new ArrayList<String>();
  }

  @Override
  public void warning(SAXParseException exception)
      throws SAXException {
    warnings.add(formatMessage(exception));
    setSuccess(false);

  }

  @Override
  public void error(SAXParseException exception)
      throws SAXException {
    errors.add(formatMessage(exception));
    setSuccess(false);
  }

  @Override
  public void fatalError(SAXParseException exception)
      throws SAXException {
    fatalErrors.add(formatMessage(exception));
    setSuccess(false);
  }

  @SuppressWarnings("unchecked")
  /**
   * Cheks if list is empty
   */
  private static final boolean isListEmpty(final List<?> list) {
    boolean isEmpty = false;
    if (list == null || list.size() == 0) {
      isEmpty = true;
    }
    return isEmpty;
  }

  @Override
  public String toString() {
    StringBuilder message = new StringBuilder("");
    if (isListEmpty(warnings) == false) {
      message.append("\n\n The following WARNINGS have been found:");
      for (String warning : warnings) {
        message.append("\n\t - " + warning);
      }
    }

    if (isListEmpty(errors) == false) {
      message.append("\n\n The following ERRORS have been found:");
      for (String error : errors) {
        message.append("\n\t - " + error);
      }
    }

    if (isListEmpty(fatalErrors) == false) {
      message.append("\n\n The following FATAL ERRORS have been found:");
      for (String fatalError : fatalErrors) {
        message.append("\n\t - " + fatalError);
      }
    }

    return message.toString();

  }

  private String formatMessage(SAXParseException exception) {
    StringBuilder message = new StringBuilder("");
    message.append("ROW: " + exception.getLineNumber());
    message.append(" COLUMN: " + exception.getColumnNumber());
    message.append(" : " + exception.getMessage());
    return message.toString();
  }

  public boolean isSuccess() {
    return success;
  }

  private void setSuccess(boolean success) {
    this.success = success;
  }

}
