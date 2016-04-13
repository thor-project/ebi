package uk.ac.ebi.thor.model.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.OrcIdRecord;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.OrcIdWorkExtIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing functions to parse a OrcId record from JSON format to OrcIdRecord object.
 * @author Guilherme Formaggio
 */
public class OrcIdJsonHelper {

  private static final Logger LOGGER = LogManager.getLogger(OrcIdJsonHelper.class);

  //Converts Java Object to JSON String
  private static ObjectMapper mapper =  new ObjectMapper();
  
  //ORCiD JSON elements
  private static final String ORCID_PROFILE = "orcid-profile";
  private static final String ORCID_IDENTIFIER = "orcid-identifier";
  private static final String ORCID_ACTIVITIES = "orcid-activities";
  private static final String ORCID_WORKS = "orcid-works";
  private static final String ORCID_WORK = "orcid-work";
  private static final String ORCID_BIO = "orcid-bio";
  private static final String PATH = "path";
  private static final String PERSONAL_DETAILS = "personal-details";
  private static final String GIVEN_NAMES = "given-names";
  private static final String FAMILY_NAME = "family-name";
  private static final String VALUE = "value";

  private OrcIdJsonHelper() {
  }

  /**
   * Parses the Json OrcId Record to a Object.
   * {"message-version":"1.2","orcid-profile":{"orcid":null,"orcid-id":null, "orcid-identifier":{
   * ... "
   * @param json
   *        String representing the OrcId Record
   * @return OrcIdRecord object
   */
  public static OrcIdRecord populateOrcIdRecord(String json)
      throws ThorServiceException {
    return populateOrcIdRecord(new OrcIdRecord(), json);
  }

  /**
   * Parses the Json OrcId Record to a Object.
   * {"message-version":"1.2","orcid-profile":{"orcid":null,"orcid-id":null, "orcid-identifier":{
   * ... "
   * @param json
   *        String representing the OrcId Record
   * @return OrcIdRecord object
   */
  public static OrcIdRecord populateOrcIdRecord(OrcIdRecord record, String json)
      throws ThorServiceException {
    LOGGER.trace("Starting populateOrcIdRecord({})", json);
    try {
      JSONObject jsonRecord = new JSONObject(json);
      JSONObject profile = jsonRecord.optJSONObject(ORCID_PROFILE);
      record.setOrcId((String) getNestedObject(profile, ORCID_IDENTIFIER, PATH));
      record.setGivenName((String) getNestedObject(profile, ORCID_BIO, PERSONAL_DETAILS,
          GIVEN_NAMES, VALUE));
      record.setFamilyName((String) getNestedObject(profile, ORCID_BIO, PERSONAL_DETAILS,
          FAMILY_NAME, VALUE));
      JSONArray orcIdWorkArray = (JSONArray) getNestedObject(profile, ORCID_ACTIVITIES,
          ORCID_WORKS,
          ORCID_WORK);
      record.setWorks(populateOrcIdWorkList(orcIdWorkArray));

    } catch (JSONException e) {
      String message = "Error converting JSON to OrcIdRecord: " + json;
      LOGGER.error(message);
      throw new ThorServiceException(message, e);
    }

    LOGGER.trace("Exiting populateOrcIdRecord()");
    return record;
  }

  /**
   * Throws an exception if the string is null or empty.
   * @param str Input String
   * @return Input String
   * @throws ThorServiceException If input string is null or empty
   */
  private static String getValidString(String str) throws ThorServiceException {
    if (str == null || "".equals(str.trim())) {
      String msg = "The String cannot be empty!";
      LOGGER.info(msg);
      throw new ThorServiceException(msg);
    }
    return str;
  }
  
  /**
   * Parses the Json from client THOR API to a Object. {"title":"My Work Title"
   * ,"workType":"journal-article","pubYear":"2001","externalIdType":"eid","externalId":"999909"}
   * @param clientJson
   *        String
   * @return OrcIdWork
   */
  public static OrcIdWork populateOrcIdWork(String clientJson)
      throws ThorServiceException {
    LOGGER.trace("Starting populateOrcIdWork({})", clientJson);
    try {
      JSONObject jsonRecord = new JSONObject(clientJson);
      String title = getValidString(jsonRecord.getString("title"));
      String workType = getValidString(jsonRecord.getString("workType"));
      String pubYear = getValidString(jsonRecord.getString("pubYear"));
      String externalIdType = getValidString(jsonRecord.getString("externalIdType"));
      String externalId = getValidString(jsonRecord.getString("externalId"));
      String url = getValidString(jsonRecord.getString("url"));
      OrcIdWork result = new OrcIdWork(title, null, workType, pubYear, externalIdType, externalId,
          url);

      LOGGER.trace("Exiting populateOrcIdRecord()");
      return result;
    } catch (JSONException e) {
      String message = "Error converting JSON to OrcIdWork: " + clientJson;
      LOGGER.error(message);
      throw new ThorServiceException(message, e);
    }
  }

  private static Object getNestedObject(JSONObject json, String... keys) {
    LOGGER.trace("Starting getNestedObject({}, {})", json, keys);
    if (json != null && keys != null) {
      //Iterates thru the keys, returning JSONObject, except the last one...
      for (int i = 0; i < keys.length - 1; i++) {
        json = json.optJSONObject(keys[i]);
        if (json == null) {
          break;
        }
      }
      //...the last key returns a JSONArray
      if (json != null) {
        Object result = json.opt(keys[keys.length - 1]);
        LOGGER.trace("Exiting getNestedObject(): {}", result);
        return result;
      }
    }
    LOGGER.trace("Exiting getNestedObject(): null");
    return null;
  }

  /**
   * Converts the Json Array "orcid-work" from "OrcId Record" to Object List.
   * {"orcid-work":[{"put-code":"496970","work-title":{"title":{"value":
   * "FutureBook"},"subtitle":{"value":"TheNextBook"},"translated-title":null}
   * ,"journal-title":{"value":"Unbelievable"},"short-description":
   * "descriptionofthecitation","work-citation":{"work-citation-type":
   * "FORMATTED_CHICAGO","citation":"mycitation"},"work-type":"BOOK",
   * "publication-date":{"year":{"value":"2014"},"month":{"value":"03"},"day":
   * {"value":"18"},"media-type":null},"work-external-identifiers":{
   * "work-external-identifier":[{"work-external-identifier-type":"ARXIV",
   * "work-external-identifier-id":{"value":"00000001111111"}}]
   * @param orcIdWorks
   *        JSONArray from OrcId-work records
   * @return List of OrcIdWork
   */
  private static List<OrcIdWork> populateOrcIdWorkList(JSONArray orcIdWorks)
      throws ThorServiceException {
    LOGGER.trace("Starting populateOrcIdWorkList({})", orcIdWorks);
    List<OrcIdWork> worksLst = null;

    if (orcIdWorks != null) {
      worksLst = new ArrayList<>(orcIdWorks.length());
      for (int i = 0; i < orcIdWorks.length(); i++) {
        try {
          OrcIdWork orcIdwork = new OrcIdWork();
          JSONObject jsonWork = orcIdWorks.getJSONObject(i);
          LOGGER.trace("Item {} converting to OrcIdWork the JSONObject {}", i, jsonWork);

          JSONObject workTitle = jsonWork.optJSONObject("work-title");
          String title = (String) getNestedObject(workTitle, "title", VALUE);
          orcIdwork.setTitle(title);
          String subtitle = (String) getNestedObject(workTitle, "subtitle", VALUE);
          orcIdwork.setSubtitle(subtitle);
          JSONArray workExtIdentifiers = (JSONArray) getNestedObject(jsonWork,
              "work-external-identifiers",
              "work-external-identifier");
          orcIdwork
              .setWorkExternalIdentifiers(populateWorkExternalIdentifierList(workExtIdentifiers));
          String workType = jsonWork.optString("work-type");
          orcIdwork.setWorkType(workType);

          worksLst.add(orcIdwork);
        } catch (JSONException e) {
          String message = "Error populating OrcId Work List for {}" + orcIdWorks;
          LOGGER.error(message, e);
          throw new ThorServiceException(message, e);
        }
      }
    }

    LOGGER.trace("Exiting populateOrcIdWorkList()");
    return worksLst;
  }

  /**
   * Converts the Json Array "Work External Identifier" from "OrcId Record" to Object List.
   * "work-external-identifier":[ { "work-external-identifier-type":"ARXIV",
   * "work-external-identifier-id":{ "value":"00000001111111" } } ]
   * @param workExtIdentifiers
   *        JSONArray from "Work External Identifier" records
   * @return List of OrcIdWorkExtIdentifier
   */
  private static List<OrcIdWorkExtIdentifier>
      populateWorkExternalIdentifierList(JSONArray workExtIdentifiers)
          throws ThorServiceException {
    LOGGER.trace("Starting populateWorkExternalIdentifierList({})", workExtIdentifiers);
    List<OrcIdWorkExtIdentifier> identifierLst = null;

    if (workExtIdentifiers != null) {
      identifierLst = new ArrayList<>(workExtIdentifiers.length());
      for (int i = 0; i < workExtIdentifiers.length(); i++) {
        try {
          JSONObject jsonWork = workExtIdentifiers.getJSONObject(i);
          LOGGER.trace("Item {} converting to OrcIdWorkExtIdentifier the JSONObject {}", i,
              jsonWork);
          OrcIdWorkExtIdentifier identifier = new OrcIdWorkExtIdentifier();
          identifier.setWorkExternalIdentifierId(
              jsonWork.getJSONObject("work-external-identifier-id").getString(VALUE));
          identifier
              .setWorkExternalIdentifierType(jsonWork.getString("work-external-identifier-type"));
          identifierLst.add(identifier);
        } catch (JSONException e) {
          String message = "Error populating OrcIdWorkExtIdentifier List for {}"
              + workExtIdentifiers;
          LOGGER.error(message);
          throw new ThorServiceException(message, e);
        }
      }
    }
    LOGGER.trace("Exiting populateWorkExternalIdentifierList(): ",
        identifierLst != null ? identifierLst.size() : null);
    return identifierLst;
  }
  
  /**
   * Converts a java object to a Json string representing it.
   * @param obj
   *        Java object
   * @return Json String for the object
   */
  public static String objectToJson(Object obj) {
    LOGGER.trace("Starting objectToJson()");
    String jsonInString = null;

    if (obj != null) {
      try {
        //Converts Java Object to JSON
        jsonInString = mapper.writeValueAsString(obj);
      } catch (JsonProcessingException e) {
        String message = String.format("Error mapping %s to JSON.", obj.getClass().getSimpleName());
        LOGGER.error(message, e);
      }
    }
    LOGGER.trace("Exiting objectToJson(): {}", jsonInString);
    return jsonInString;
  }

}
