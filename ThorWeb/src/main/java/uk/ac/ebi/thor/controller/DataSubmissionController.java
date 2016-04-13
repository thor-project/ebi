package uk.ac.ebi.thor.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.thor.service.OrcIdService;

@RestController
@RequestMapping("/api/orcid")
public class DataSubmissionController {

  private static final Logger LOGGER = LogManager.getLogger(DataSubmissionController.class);

  @Autowired
  OrcIdService service;

  /**
   * Service to allow clients to search for ORCID Complete Registry based on key words.
   * @param keyword
   *        Keyword to search for ORCID
   * @return JSON from ORCID search webservice, with all data fields available
   */
  @RequestMapping(value = "/find/{keyword}", method = RequestMethod.GET)
  public String findAllRecordsOrcIds(@PathVariable String keyword) {
    LOGGER.debug("Starting findAllRecordsOrcIds({})", keyword);
    String result = service.searchOrcIdsAllRecords(keyword);
    LOGGER.debug("Ending findAllRecordsOrcIds: {}", result);
    return result;
  }
}
