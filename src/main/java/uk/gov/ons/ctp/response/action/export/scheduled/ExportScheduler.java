package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

/** This class will be responsible for the scheduling of export actions */
@Component
public class ExportScheduler {
  private static final Logger log = LoggerFactory.getLogger(ExportScheduler.class);

  public static final String ACTION_EXECUTION_LOCK = "actionexport.request.execution";

  private final TemplateMappingService templateMappingService;

  private final NotificationFileCreator notificationFileCreator;

  private final ActionRequestRepository actionRequestRepository;

  private final RedissonClient redissonClient;

  private final AppConfig appConfig;

  public ExportScheduler(
      TemplateMappingService templateMappingService,
      NotificationFileCreator notificationFileCreator,
      ActionRequestRepository actionRequestRepository,
      RedissonClient redissonClient,
      AppConfig appConfig) {
    this.templateMappingService = templateMappingService;
    this.notificationFileCreator = notificationFileCreator;
    this.actionRequestRepository = actionRequestRepository;
    this.redissonClient = redissonClient;
    this.appConfig = appConfig;
  }

  /** Carry out scheduled actions according to configured cron expression */
  @Scheduled(cron = "#{appConfig.exportSchedule.cronExpression}")
  public void scheduleExport() {
    log.debug("Scheduled run start");
    RLock lock = redissonClient.getFairLock(ACTION_EXECUTION_LOCK);
    try {
      // Get an EXCLUSIVE lock so hopefully only one thread/process is ever writing files to the
      // SFTP server. Automatically unlock after a certain amount of time to prevent
      // issues when lock holder crashes or Redis crashes causing permanent lockout
      if (lock.tryLock(appConfig.getDataGrid().getLockTimeToLiveSeconds(), TimeUnit.SECONDS)) {
        try {
          processExport();
        } finally {
          // Always unlock the distributed lock
          lock.unlock();
        }
      }
    } catch (InterruptedException e) {
      // Ignored - process stopped while waiting for lock
    }
  }

  private void processExport() {
    final List<SurveyRefExerciseRef> exerciseRefs =
        actionRequestRepository.findDistinctSurveyAndExerciseRefs();

    for (final SurveyRefExerciseRef exerciseRef : exerciseRefs) {
      processActionRequestsForCollectionExercise(exerciseRef);
    }
  }

  private void processActionRequestsForCollectionExercise(
      final SurveyRefExerciseRef surveyRefExerciseRef) {
    templateMappingService
        .retrieveAllTemplateMappingsByFilename()
        .forEach(publishFile(surveyRefExerciseRef));
  }

  private BiConsumer<String, List<TemplateMapping>> publishFile(
      final SurveyRefExerciseRef surveyRefExerciseRef) {
    return (fileName, templateMappings) -> {
      final String filenamePrefix =
          fileName
              + "_"
              + surveyRefExerciseRef.getSurveyRef()
              + "_"
              + surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();
      log.with("survey", surveyRefExerciseRef.getSurveyRef())
          .with("exercise_ref", surveyRefExerciseRef.getExerciseRef())
          .info("Publishing notification file");
      notificationFileCreator.publishNotificationFile(
          surveyRefExerciseRef, templateMappings, filenamePrefix);
    };
  }
}
