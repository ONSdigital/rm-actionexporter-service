package uk.gov.ons.ctp.response.action.export.endpoint;

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
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportProcessor;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportScheduler;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/delete", produces = "application/json")
public class DeleteEndpoint {
  private static final Logger log = LoggerFactory.getLogger(DeleteEndpoint.class);

  @Autowired private ActionRequestRepository actionRequestRepository;

  @Autowired private ExportFileRepository exportFileRepository;

  @RequestMapping(method = RequestMethod.DELETE)
  public ResponseEntity<String> triggerDelete() throws CTPException {
    try {
      List<ExportFile> exportFiles = exportFileRepository.findAll();
      for (ExportFile exportFile: exportFiles) {
        Timestamp dateSuccessfullySent = exportFile.getDateSuccessfullySent();
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date ninetyDaysAgo = new Date(System.currentTimeMillis() - (90 * DAY_IN_MS));

        if (dateSuccessfullySent != null && dateSuccessfullySent.before(ninetyDaysAgo)) {
          Stream<ActionRequestInstruction> actionRequestInstructions = actionRequestRepository.findByExportJobId(
                  exportFile.getExportJobId());

          actionRequestInstructions.forEach(
            ari -> {
              actionRequestRepository.delete(ari);
            });
          exportFileRepository.delete(exportFile);
        }
      }

      return ResponseEntity.ok().body("Delete completed - TODO better info");
    } catch (Exception e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when exporting print file");
    }
  }
}
