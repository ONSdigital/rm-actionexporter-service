package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

/** This class will be responsible for the scheduling of export actions */
@Component
public class ExportScheduler {
  private static final Logger log = LoggerFactory.getLogger(ExportScheduler.class);

  private TemplateMappingService templateMappingService;

  private NotificationFileCreator notificationFileCreator;

  private ActionRequestService actionRequestService;

  private DistributedLockManager actionExportLockManager;

  @Autowired
  public ExportScheduler(
      TemplateMappingService templateMappingService,
      NotificationFileCreator notificationFileCreator,
      ActionRequestService actionRequestService,
      DistributedLockManager actionExportLockManager) {
    this.templateMappingService = templateMappingService;
    this.notificationFileCreator = notificationFileCreator;
    this.actionRequestService = actionRequestService;
    this.actionExportLockManager = actionExportLockManager;
  }

  /** Carry out scheduled actions according to configured cron expression */
  @Scheduled(cron = "#{appConfig.exportSchedule.cronExpression}")
  public void scheduleExport() {
    log.info("Scheduled run start");
    final List<SurveyRefExerciseRef> exerciseRefs =
        actionRequestService.retrieveDistinctExerciseRefsWithSurveyRef();

    for (final SurveyRefExerciseRef exerciseRef : exerciseRefs) {
      processActionRequestsForCollectionExercise(exerciseRef);
    }
  }

  /**
   * Deal with actionRequests for a collection exercise. Lock on file being created so only one
   * instance can write to an SFTP file. Deal with exerciseRefs separately as a file is created for
   * each collection exercise with filename from the lookup table as a prefix and exerciseRef
   *
   * @param surveyRefExerciseRef collection exercise to deal with.
   */
  private void processActionRequestsForCollectionExercise(
      final SurveyRefExerciseRef surveyRefExerciseRef) {
    final String surveyRefAndExerciseRef =
        surveyRefExerciseRef.getSurveyRef()
            + "_"
            + surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();

    templateMappingService
        .retrieveAllTemplateMappingsByFilename()
        .forEach(
            (fileName, templateMappings) -> {
              final String filenamePrefix = fileName + "_" + surveyRefAndExerciseRef;
              final boolean isLockedAlready = actionExportLockManager.isLocked(filenamePrefix);
              try {
                final boolean gotLock = actionExportLockManager.lock(filenamePrefix);
                if (isLockedAlready || !gotLock) {
                  log.with("filename_with_exercise_ref", filenamePrefix)
                      .with("file_locked", isLockedAlready)
                      .debug("Could not get a lock");
                  return;
                }
                notificationFileCreator.publishNotificationFile(
                    surveyRefExerciseRef, templateMappings, filenamePrefix);
              } finally {
                actionExportLockManager.unlock(filenamePrefix);
              }
            });
  }
}
