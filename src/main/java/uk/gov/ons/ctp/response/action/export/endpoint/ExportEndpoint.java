package uk.gov.ons.ctp.response.action.export.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportScheduler;

@RestController
@RequestMapping(value = "/export", produces = "application/json")
public class ExportEndpoint {
  private static final Logger log = LoggerFactory.getLogger(ExportEndpoint.class);

  @Autowired private final ExportScheduler exportScheduler;

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<String> triggerExport() throws CTPException {
    try {
      exportScheduler.scheduleExport();
      return ResponseEntity.ok().body("Export started");
    } catch (Exception e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when exporting print file");
    }
  }
}
