package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

import java.util.List;
import java.util.function.BiConsumer;

/** This class will be responsible for the scheduling of export actions */
@Component
public class ExportScheduler {
  private static final Logger log = LoggerFactory.getLogger(ExportScheduler.class);

  private final TemplateMappingService templateMappingService;

  private final NotificationFileCreator notificationFileCreator;

  private final ActionRequestService actionRequestService;

  private final DistributedLockManager actionExportLockManager;

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

  private void processActionRequestsForCollectionExercise(
      final SurveyRefExerciseRef surveyRefExerciseRef) {
    final String surveyRefAndExerciseRef =
        surveyRefExerciseRef.getSurveyRef()
            + "_"
            + surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();

    templateMappingService
        .retrieveAllTemplateMappingsByFilename()
        .forEach(publishFile(surveyRefExerciseRef, surveyRefAndExerciseRef));
  }

  /**
   * Lock on file being created so only one instance can write to an SFTP file.
   */
  private BiConsumer<String, List<TemplateMapping>> publishFile(
      final SurveyRefExerciseRef surveyRefExerciseRef, final String surveyRefAndExerciseRef) {
    return (fileName, templateMappings) -> {
      final String filenamePrefix = fileName + "_" + surveyRefAndExerciseRef;
      final boolean isLockedAlready = actionExportLockManager.isLocked(filenamePrefix);
      final boolean gotLock = !isLockedAlready && actionExportLockManager.lock(filenamePrefix);
      if (!gotLock) {
        log.with("filename_with_exercise_ref", filenamePrefix)
            .with("file_locked", isLockedAlready)
            .debug("Could not get a lock");
        return;
      }
      try {
        notificationFileCreator.publishNotificationFile(
            surveyRefExerciseRef, templateMappings, filenamePrefix);
      } finally {
        actionExportLockManager.unlock(filenamePrefix);
      }
    };
  }
}
