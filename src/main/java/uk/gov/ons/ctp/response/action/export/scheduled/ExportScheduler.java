package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;

/** This class will be responsible for the scheduling of export actions */
@Component
public class ExportScheduler {
  private static final Logger log = LoggerFactory.getLogger(ExportScheduler.class);

  private static final String ACTION_EXECUTION_LOCK = "actionexport.request.execution";

  private final ExportProcessor exportProcessor;

  private final RedissonClient redissonClient;

  private final AppConfig appConfig;

  public ExportScheduler(
      ExportProcessor exportProcessor, RedissonClient redissonClient, AppConfig appConfig) {
    this.exportProcessor = exportProcessor;
    this.redissonClient = redissonClient;
    this.appConfig = appConfig;
  }

  // This is called using a K8s CronJob via /export
  public void scheduleExport() throws Exception {
    log.debug("Scheduled run start");
    processExport();
  }

  private void processExport() {
    RLock lock = redissonClient.getFairLock(ACTION_EXECUTION_LOCK);
    try {
      // Get an EXCLUSIVE lock so hopefully only one thread/process is ever writing files to the
      // SFTP server. Automatically unlock after a certain amount of time to prevent
      // issues when lock holder crashes or Redis crashes causing permanent lockout
      if (lock.tryLock(appConfig.getDataGrid().getLockTimeToLiveSeconds(), TimeUnit.SECONDS)) {
        try {
          exportProcessor.processExport();
        } finally {
          // Always unlock the distributed lock
          lock.unlock();
        }
      }
    } catch (InterruptedException e) {
      // Ignored - process stopped while waiting for lock
    }
  }
}
