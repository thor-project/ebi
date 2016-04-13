package uk.ac.ebi.thor.model.helper;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.OrcIdWorkExtIdentifier;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class OrcIdXmlHelper {

  private static final Logger LOGGER = LogManager.getLogger(OrcIdXmlHelper.class);
  private static final String NL_XML = "\n";
  private static final String TAB_XML = "\t";

  private OrcIdXmlHelper() {
  }

  /**
   * Creates an XML to be posted for the OrcId web service to add new work.
   * <?xml version="1.0" encoding="UTF-8"?>
   * <orcid-message xmlns="http://www.orcid.org/ns/orcid">
   *     <message-version>1.2</message-version>
   *     <orcid-profile>
   *         <orcid-activities>
   *             <orcid-works>
   *                 <orcid-work>
   *                     <work-title>
   *                         <title>My Work Title</title>
   *                     </work-title>
   *                     <work-type>journal-article</work-type>
   *                     <publication-date>
   *                         <year>2001</year>
   *                     </publication-date>
   *                     <work-external-identifiers>
   *                         <work-external-identifier>
   *                             <work-external-identifier-type>eid</work-external-identifier-type>
   *                             <work-external-identifier-id>999909</work-external-identifier-id>
   *                         </work-external-identifier>
   *                     </work-external-identifiers>
   *                     <url>http://nonono.com</url>
   *                 </orcid-work>
   *             </orcid-works>
   *         </orcid-activities>
   *     </orcid-profile>
   * </orcid-message>
   * 
   * @param orcIdWork Work details
   * @return xml valid for orcid
   * @throws ThorServiceException If valid XML cannot be created
   */
  public static String getAddWorksXml(OrcIdWork orcIdWork)
      throws ThorServiceException {
    LOGGER.trace("Starting getAddWorksXml()");
    StringBuilder xml = new StringBuilder(getOrcidMessageHeader());
    xml.append(NL_XML + TAB_XML + "<orcid-profile>");
    xml.append(NL_XML + TAB_XML + TAB_XML + "<orcid-activities>");
    xml.append(NL_XML + TAB_XML + TAB_XML + TAB_XML + "<orcid-works>");
    xml.append(getWorkXml(orcIdWork, 4));
    xml.append(NL_XML + TAB_XML + TAB_XML + TAB_XML + "</orcid-works>");
    xml.append(NL_XML + TAB_XML + TAB_XML + "</orcid-activities>");
    xml.append(NL_XML + TAB_XML + "</orcid-profile>");
    xml.append(getOrcidMessageFooter());

    String result = xml.toString();
    if (validateXml(result)) {
      LOGGER.trace("Exiting getAddWorksXml(): {}", result);
      return result;
    } else {
      String msg = "Error creating XML from OrcIdWork object.";
      LOGGER.error(msg);
      throw new ThorServiceException(msg);
    }
  }

  private static String getOrcidMessageHeader() {
    String messageHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + NL_XML + "<orcid-message xmlns=\"http://www.orcid.org/ns/orcid\">"
        + NL_XML + TAB_XML + "<message-version>1.2</message-version>";
    return messageHeader;
  }

  private static String getOrcidMessageFooter() {
    String messageFooter = NL_XML + "</orcid-message>";
    return messageFooter;
  }

  /**
   * Getter of the textual xml tag with the information passed (only text inside without attributes.
   * and children)
   * @param name
   *        the name of the tag
   * @param value
   *        the value of the tag
   * @param deep
   *        number of the identations in the XML (useful for formatting the XML)
   * @return the xml tag with the information passed
   */
  private static String getXmlTag(String name, String value, int deep) {
    if (value == null) {
      value = "";
    }
    
    StringBuilder ret = new StringBuilder(NL_XML).append(StringUtils.repeat(TAB_XML, deep))
        .append("<").append(name).append(">")
        .append(StringEscapeUtils.escapeXml(value.toString())).append("</").append(name)
        .append(">");

    return ret.toString();
  }

  /**
   * Create the orcid-work element and it's contents with work details.
   * 
   *                 <orcid-work>
   *                     <work-title>
   *                         <title>My Work Title</title>
   *                     </work-title>
   *                     <work-type>journal-article</work-type>
   *                     <publication-date>
   *                         <year>2001</year>
   *                     </publication-date>
   *                     <work-external-identifiers>
   *                         <work-external-identifier>
   *                             <work-external-identifier-type>eid</work-external-identifier-type>
   *                             <work-external-identifier-id>999909</work-external-identifier-id>
   *                         </work-external-identifier>
   *                     </work-external-identifiers>
   *                     <url>http://nonono.com</url>
   *                 </orcid-work>
   * 
   * @param orcIdWork OrcIdWork
   * @param tabIndex Number of tabs in the beginning of xml
   * @return XML for orcid-work
   */
  private static String getWorkXml(OrcIdWork orcIdWork, int tabIndex) {
    LOGGER.trace("Starting getWorkXml()");
    StringBuilder xml = new StringBuilder();

    if (orcIdWork != null) {
      String prefixTab = NL_XML + StringUtils.repeat(TAB_XML, tabIndex);
      xml.append(prefixTab + "<orcid-work>");

      xml.append(prefixTab + TAB_XML + "<work-title>");
      xml.append(getXmlTag("title", orcIdWork.getTitle(), tabIndex + 2));
      xml.append(prefixTab + TAB_XML + "</work-title>");

      xml.append(getXmlTag("work-type", orcIdWork.getWorkType(), tabIndex + 1));

      xml.append(prefixTab + TAB_XML + "<publication-date>");
      xml.append(getXmlTag("year", orcIdWork.getPublicationYear(), tabIndex + 2));
      xml.append(prefixTab + TAB_XML + "</publication-date>");

      xml.append(prefixTab + TAB_XML + "<work-external-identifiers>");
      if (orcIdWork.getWorkExternalIdentifiers() != null) {
        for (OrcIdWorkExtIdentifier extId : orcIdWork.getWorkExternalIdentifiers()) {
          xml.append(prefixTab + TAB_XML + TAB_XML + "<work-external-identifier>");
          xml.append(getXmlTag("work-external-identifier-type",
              extId.getWorkExternalIdentifierType(), tabIndex + 3));
          xml.append(getXmlTag("work-external-identifier-id", extId.getWorkExternalIdentifierId(),
              tabIndex + 3));
          xml.append(prefixTab + TAB_XML + TAB_XML + "</work-external-identifier>");
        }
      }
      xml.append(prefixTab + TAB_XML + "</work-external-identifiers>");

      xml.append(getXmlTag("url", orcIdWork.getUrl(), tabIndex + 1));

      xml.append(prefixTab + "</orcid-work>");
    }

    LOGGER.trace("Exiting getWorkXml():{}", xml.toString());
    return xml.toString();
  }

  /**
   * Validate if the XML is a valid OrcId payload.
   * @param xml
   *        String
   * @throws ThorServiceException
   *         In case the xml is not valid
   */
  private static boolean validateXml(String xml) {
    LOGGER.trace("Starting validateXml({})", xml);
    try {
      SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      ClassLoader classLoader = OrcIdXmlHelper.class.getClassLoader();
      File file = new File(classLoader.getResource("orcid/orcid_xml_validation.xsd").getFile());
      Schema schema = factory.newSchema(
          new StreamSource(file));
      Validator validator = schema.newValidator();
      Source source = new StreamSource(new StringReader(xml.toString()));
      OrcidXmlErrorHandler errorHandler = new OrcidXmlErrorHandler();
      validator.setErrorHandler(errorHandler);
      validator.validate(source);
      if (errorHandler.isSuccess()) {
        LOGGER.trace("Exiting validateXml(): true");
        return true;
      }
      LOGGER.error("Problems during the foolowing XML ORCID validation: {} \n {} ", xml,
          errorHandler.toString());
    } catch (SAXException e) {
      LOGGER.error("Problems during the foolowing XML ORCID validation: {} \n {} ", xml,
          e.getMessage());
    } catch (IOException e) {
      LOGGER.error("Problems during the foolowing XML ORCID validation: {} \n {} ", xml,
          e.getMessage());
    }
    LOGGER.trace("Exiting validateXml(): false");
    return false;
  }

}
