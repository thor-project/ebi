package uk.ac.ebi.thor.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.thor.exception.NonAuthorizedThorException;
import uk.ac.ebi.thor.exception.ThorServiceException;
import uk.ac.ebi.thor.model.DataClaimingResponse;
import uk.ac.ebi.thor.model.OrcIdRecord;
import uk.ac.ebi.thor.model.OrcIdWork;
import uk.ac.ebi.thor.model.OrcIdWorkExtIdentifier;
import uk.ac.ebi.thor.model.SessionUser;
import uk.ac.ebi.thor.model.helper.OrcIdJsonHelper;
import uk.ac.ebi.thor.service.DataClaimingService;

/**
 * Class exposing the Rest WebServices related to Data Claiming.
 * @author Guilherme Formaggio
 */
@RestController
@RequestMapping("/api/dataclaiming")
public class DataClaimingController {

  private static final Logger LOGGER = LogManager.getLogger(DataClaimingController.class);

  @Autowired
  DataClaimingService service;

  /**
   * Claim data, adding a new OrcId Work record.
   * @param request
   *        HttpServletRequest
   * @param ordIdWorkJson
   *        Work details
   * @throws ThorServiceException
   *         In case of error
   */
  public ResponseEntity<String> claimNewOrcIdWork(HttpServletRequest request, @RequestParam(
      value = "ordIdWorkJson") String ordIdWorkJson) {
    LOGGER.trace("Starting claimNewOrcIdWork({},{})", request,
        ordIdWorkJson);
    try {
      //work id of current data being displayed to the user. 
      //This value is mandatory.
      OrcIdWork orcIdWork = OrcIdJsonHelper.populateOrcIdWork(ordIdWorkJson);
      //Add work to user OrcId record
      service.claimOrcIdWork(request, orcIdWork);
      LOGGER.trace("Exiting claimNewOrcIdWork() with HttpStatus.OK");
      return new ResponseEntity<String>("", HttpStatus.OK);
    } catch (ThorServiceException e) {
      String message = "Error occurred when claiming work to OrcId.";
      LOGGER.error(message, e);
      return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns the claiming informations for the current user, such as: is logged in, name, if current
   * data is already claimed or not.
   * @param request
   *        HttpServletRequest
   * @return DataClaimingResponse as JSON format
   */
  @RequestMapping(value = "/claiming",
      method = RequestMethod.GET,
      produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<String>
      getClaimingInfo(HttpServletRequest request, HttpServletResponse resp, @RequestParam(
          value = "ordIdWorkJson") String ordIdWorkJson) {
    LOGGER.trace("Entering getClaimingInfo({})", request.getSession().getId());

    //Services to fill the response
    DataClaimingResponse dataClaimingResponse = new DataClaimingResponse();
    SessionUser sessionUser = service.retrieveUserSession(request);
    dataClaimingResponse.setIsUserLoggedIn(service.isUserLoggedIn(sessionUser));
    dataClaimingResponse.setLoginUrl(service.getLoginUrl(request));
    dataClaimingResponse.setLogoutUrl(service.getLogoutUrl(request));

    //work id of current data being displayed to the user. 
    //This value is mandatory.
    OrcIdWork orcIdWork = null;
    try {
      orcIdWork = OrcIdJsonHelper.populateOrcIdWork(ordIdWorkJson);
    } catch (ThorServiceException e) {
      //If it is not possible to populae the orcIdWork, it means the
      //Thor Client is missconfigured.
      LOGGER.error(e);
      return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    OrcIdWorkExtIdentifier workId = orcIdWork.getWorkExternalIdentifiers().get(0);

    //checks if current Data is already claimed to user's OrcId Record
    OrcIdRecord orcIdRecord;
    try {
      orcIdRecord = service.loadOrcIdRecord(request);
      if (orcIdRecord != null) {
        dataClaimingResponse.setOrcIdRecord(orcIdRecord);
        dataClaimingResponse.setIsDataClaimed(orcIdRecord.hasWorkIdentifier(workId));
      }
    } catch (ThorServiceException e) {
      LOGGER.error(e);
    } catch (NonAuthorizedThorException e) {
      //If user credentials are not valid, we must discard and make the user log in again at OrcId
      dataClaimingResponse.setIsUserLoggedIn(false);
      service.newUserSession(request.getSession());
      //Delete the remember me cookie, since the credentials stored are invalid
      service.registerCookie(request, resp);
    }

    //Maps object to json
    String jsonInString = null;
    jsonInString = OrcIdJsonHelper.objectToJson(dataClaimingResponse);

    LOGGER.trace("Exiting getClaimingInfo(): {}", jsonInString);
    return new ResponseEntity<String>(jsonInString, HttpStatus.OK);
  }

  /**
   * Returns the login URL to authenticate within OrcID data is already claimed or not.
   * @param request
   *        HttpServletRequest
   * @return Login url
   */
  @RequestMapping(value = "/loginUrl",
      method = RequestMethod.GET,
      produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<String>
      getLoginUrl(HttpServletRequest request) {
    LOGGER.trace("Starting getLoginUrl({})", request);
    String loginUrl = service.getLoginUrl(request);
    LOGGER.trace("Exiting getLoginUrl(): {}", loginUrl);
    return new ResponseEntity<String>(loginUrl, HttpStatus.OK);
  }

  /**
   * Returns the user bio informations from orcid directly.
   * @param request HttpServletRequest
   * @return Json from orcid bio
   */
  @RequestMapping(value = "/orcIdBio",
      method = RequestMethod.GET,
      produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<String> getOrcIdBio(HttpServletRequest request) {
    LOGGER.trace("Starting getOrcIdBio({})", request);
    ResponseEntity<String> result = null;
    //Identify de user
    SessionUser sessionUser = service.retrieveUserSession(request);
    //If user has authenticated at orcid, get his bio
    if (service.isUserLoggedIn(sessionUser)) {
      String orcIdBioJson = service.getOrcIdBio(sessionUser.getOrcIdRecord().getOrcId());
      //create webservice response
      result = new ResponseEntity<String>(orcIdBioJson, HttpStatus.OK);
    }

    //return results
    LOGGER.trace("Exiting getOrcIdBio(): {}", result);
    return result;
  }
}
