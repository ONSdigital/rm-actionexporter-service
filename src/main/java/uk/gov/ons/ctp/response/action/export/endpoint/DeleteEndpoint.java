package uk.gov.ons.ctp.response.action.export.endpoint;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.scheduled.DeleteProcessor;

@RestController
@RequestMapping(value = "/delete-old-data", produces = "application/json")
public class DeleteEndpoint {
  private static final Logger log = LoggerFactory.getLogger(DeleteEndpoint.class);

  @Autowired private DeleteProcessor deleteProcessor;

  @RequestMapping(method = RequestMethod.DELETE)
  public ResponseEntity<String> triggerDelete() throws CTPException {
    try {
      log.info("Beginning deletion of old records");
      List<ExportJob> exportJobs = deleteProcessor.getAllExportJobIdsForDeletion();

      for (ExportJob exportJob : exportJobs) {
        deleteProcessor.triggerDeleteForExportJob(exportJob);
      }
      log.info("Completed deletion of old records. [" + exportJobs.size() + "] exportJobs deleted");
      return ResponseEntity.ok()
          .body(
              "Completed deletion of old records. [" + exportJobs.size() + "] exportJobs deleted");
    } catch (RuntimeException e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when deleting old records");
    }
  }
}
