package uk.gov.ons.ctp.response.action.export.endpoint;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.AddressRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;

@RestController
@RequestMapping(value = "/delete", produces = "application/json")
public class DeleteEndpoint {
  private static final Logger log = LoggerFactory.getLogger(DeleteEndpoint.class);
  private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

  @Autowired private ActionRequestRepository actionRequestRepository;
  @Autowired private ExportJobRepository exportJobRepository;
  @Autowired private ExportFileRepository exportFileRepository;

  @RequestMapping(method = RequestMethod.DELETE)
  @Transactional
  public ResponseEntity<String> triggerDelete() throws CTPException {
    try {
      List<ExportJob> exportJobs = exportJobRepository.findAll();
      log.info("Found [" + exportJobs.size() + "] exportJobIds to process");

      for (ExportJob exportJob : exportJobs) {
        List<ExportFile> exportFiles = exportFileRepository.findAllByExportJobId(exportJob.getId());
        log.info(
            "Found ["
                + exportFiles.size()
                + "] files for the exportJobId ["
                + exportJob.getId()
                + "]");
        // If the exportJob has any files unsent or younger then 90 days, we don't want to remove
        // the linking id.
        boolean allFilesFromExportJobSent = true;

        for (ExportFile exportFile : exportFiles) {
          log.info("Working on exportFile with id [" + exportFile.getId() + "]");
          Timestamp dateSuccessfullySent = exportFile.getDateSuccessfullySent();
          Date ninetyDaysAgo = new Date(System.currentTimeMillis() - (90 * DAY_IN_MS));

          if (dateSuccessfullySent != null && dateSuccessfullySent.before(ninetyDaysAgo)) {
            log.info(
                "exportFile ["
                    + exportFile.getId()
                    + "] is older then 90 days.  Deleting all associated "
                    + "actionRequests and the exportFile row.");
            Stream<ActionRequestInstruction> actionRequestInstructions =
                actionRequestRepository.findByExportJobId(exportFile.getExportJobId());

            actionRequestInstructions.forEach(
                ari -> {
                  actionRequestRepository.delete(ari);
                  log.info("Deleted action request row [" + ari.getActionrequestPK() + "]");
                });
            exportFileRepository.delete(exportFile);
            log.info("Deleted exportFile row [" + exportFile.getId() + "]");
          } else {
            log.info(
                "Not deleting exportFile ["
                    + exportFile.getId()
                    + "]. It either hasn't been processed or is "
                    + "less than 90 days old");
            allFilesFromExportJobSent = false;
          }
        }

        // If we've deleted all records associated with this exportJobId, then we delete it because
        // it's not useful.
        if (allFilesFromExportJobSent) {
          exportJobRepository.deleteById(exportJob.getId());
          log.info(
              "Deleted exportJob row ["
                  + exportJob.getId()
                  + "] as all associated data has been deleted");
        }
      }

      return ResponseEntity.ok().body("Delete completed - TODO better info");
    } catch (Exception e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when deleting old records");
    }
  }
}
