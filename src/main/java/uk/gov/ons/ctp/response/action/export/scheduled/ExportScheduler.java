package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.distributed.DistributedInstanceManager;
import uk.gov.ons.ctp.common.distributed.DistributedLatchManager;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.ExportReportService;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;
import uk.gov.ons.ctp.response.action.export.service.TransformationService;

/** This class will be responsible for the scheduling of export actions */
@Component
public class ExportScheduler implements HealthIndicator {
  private static final Logger log = LoggerFactory.getLogger(ExportScheduler.class);

  private static final String DATE_FORMAT_IN_FILE_NAMES = "ddMMyyyy_HHmm";
  private static final String DISTRIBUTED_OBJECT_KEY_FILE_LATCH = "filelatch";
  private static final String DISTRIBUTED_OBJECT_KEY_REPORT_LATCH = "reportlatch";
  private static final String DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT = "scheduler";
  private static final String DISTRIBUTED_OBJECT_KEY_REPORT = "report";

  @Autowired private TransformationService transformationService;

  @Autowired private TemplateMappingService templateMappingService;

  @Autowired private SftpServicePublisher sftpService;

  @Autowired private ActionRequestService actionRequestService;

  @Autowired private DistributedLockManager actionExportLockManager;

  @Autowired private DistributedInstanceManager actionExportInstanceManager;

  @Autowired private DistributedLatchManager actionExportLatchManager;

  @Autowired private ExportReportService exportReportService;

  @Autowired private EventPublisher eventPublisher;

  @Autowired private ExportInfo exportInfo;

  @Override
  public Health health() {
    return Health.up().withDetail("exportInfo", exportInfo).build();
  }

  /** Initialise export scheduler */
  @PostConstruct
  public void init() {
    actionExportInstanceManager.incrementInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
    log.info(
        "{} {} instance/s running",
        actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT),
        DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
  }

  /** Clean up scheduler on bean destruction */
  @PreDestroy
  public void cleanUp() {
    actionExportInstanceManager.decrementInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
    // Make sure no locks if interrupted in middle of run
    actionExportLockManager.unlockInstanceLocks();
    log.info(
        "{} {} instance/s running",
        actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT),
        DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
  }

  /** Carry out scheduled actions according to configured cron expression */
  @Scheduled(cron = "#{appConfig.exportSchedule.cronExpression}")
  public void scheduleExport() {
    log.info("Scheduled run start");

    // Warn if Mapping document cannot deal with all ActionRequests stored
    List<String> storedActionTypes = actionRequestService.retrieveActionTypes();
    List<String> mappedActionTypes = templateMappingService.retrieveActionTypes();
    storedActionTypes.forEach(
        (actionType) -> {
          if (!mappedActionTypes.contains(actionType)) {
            log.with("action_type", actionType).warn("No mapping for actionType");
          }
        });

    actionExportLatchManager.setCountDownLatch(
        DISTRIBUTED_OBJECT_KEY_FILE_LATCH,
        actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT));

    List<SurveyRefExerciseRef> exerciseRefs =
        actionRequestService.retrieveDistinctExerciseRefsWithSurveyRef();

    exerciseRefs.forEach(
        (exerciseRef) -> {
          sendExport(exerciseRef);
          eventPublisher.publishEvent("Print file");
        });

    // Wait for all instances to finish to synchronise the removal of locks
    try {
      actionExportLatchManager.countDown(DISTRIBUTED_OBJECT_KEY_FILE_LATCH);
      if (!actionExportLatchManager.awaitCountDownLatch(DISTRIBUTED_OBJECT_KEY_FILE_LATCH)) {
        log.with(
                "instances_running",
                actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT))
            .error("Scheduled run error countdownlatch timed out, should be instances running");
      }
    } catch (InterruptedException e) {
      log.error("Scheduled run error waiting for countdownlatch", e);
    } finally {
      actionExportLockManager.unlockInstanceLocks();
      actionExportLatchManager.deleteCountDownLatch(DISTRIBUTED_OBJECT_KEY_FILE_LATCH);
      log.info(
          "{} {} instance/s running",
          actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT),
          DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT);
    }

    if (!createReport()) {
      log.error("Scheduled run error creating report");
    }
  }

  /**
   * Deal with actionRequests for a collection exercise. Lock on file being created so only one
   * instance can write to an SFTP file. Deal with exerciseRefs separately as a file is created for
   * each collection exercise with filename from the lookup table as a prefix and exerciseRef
   *
   * @param surveyRefExerciseRefTuple collection exercise to deal with.
   */
  private void sendExport(SurveyRefExerciseRef surveyRefExerciseRefTuple) {

    String exerciseRef = correctExRefFormat(surveyRefExerciseRefTuple);

    final String surveyRefAndexerciseRef =
        surveyRefExerciseRefTuple.getSurveyRef() + "_" + exerciseRef;

    // Process templateMappings by file to be created, have to as may be many
    // actionTypes in one file. Does not assume actionTypes in the same file use
    // the same template even so.
    String timeStamp =
        new SimpleDateFormat(DATE_FORMAT_IN_FILE_NAMES).format(Calendar.getInstance().getTime());

    templateMappingService
        .retrieveAllTemplateMappingsByFilename()
        .forEach(
            (fileName, templatemappings) -> {
              String fileNameWithExerciseRef = fileName + "_" + surveyRefAndexerciseRef;
              log.with("filename_with_exercise_ref", fileNameWithExerciseRef)
                  .with("file_locked", actionExportLockManager.isLocked(fileNameWithExerciseRef))
                  .debug("Lock test");

              if (!actionExportLockManager.isLocked(fileNameWithExerciseRef)
                  && actionExportLockManager.lock(fileNameWithExerciseRef)) {

                log.with("filename_with_exercise_ref", fileNameWithExerciseRef)
                    .with("file_locked", actionExportLockManager.isLocked(fileNameWithExerciseRef))
                    .debug("Lock test");
                ExportMessage message = new ExportMessage();

                // process Collection of templateMappings
                templatemappings.forEach(
                    (templateMapping) -> {
                      List<ActionRequestInstruction> requests =
                          actionRequestService.findByDateSentIsNullAndActionTypeAndExerciseRef(
                              templateMapping.getActionType(),
                              surveyRefExerciseRefTuple.getExerciseRef(),
                              surveyRefExerciseRefTuple.getSurveyRef());
                      if (requests.isEmpty()) {
                        log.with("action_type", templateMapping.getActionType())
                            .with("survey_ref", surveyRefExerciseRefTuple.getExerciseRef())
                            .with("exercise_ref", surveyRefExerciseRefTuple.getSurveyRef())
                            .info(
                                "No requests for actionType, surveyRef and exerciseRef to process");
                      } else {
                        try {
                          transformationService.processActionRequests(message, requests);
                        } catch (CTPException e) {
                          log.error("Scheduled run error transforming ActionRequests", e);
                        }
                      }
                    });

                if (!message.isEmpty()) {
                  sftpService.sendMessage(
                      fileNameWithExerciseRef + "_" + timeStamp + ".csv",
                      message.getMergedActionRequestIdsAsStrings(),
                      message.getMergedOutputStreams());
                }
              }
            });
  }

  // This checks the format of exerciseRef if it is survey_ref + exercise_ref e.g 221_2017_12
  // it strips the survey_ref off. This is because the exercise_ref is set incorrectly in Collection
  // Exercise service.
  // This data is stored in the actionexporter.actionrequest table.
  // TODO: Remove this code when production exerciseRef data is fixed.
  private String correctExRefFormat(SurveyRefExerciseRef surveyRefExerciseRefTuple) {
    String exerciseRef = surveyRefExerciseRefTuple.getExerciseRef();
    String afterUnderscore = StringUtils.substringAfterLast(exerciseRef, "_");
    return StringUtils.defaultIfEmpty(afterUnderscore, exerciseRef);
  }

  /**
   * Create an entry for the report service for exports created. Have to wait for all potential
   * service instances to finish the scheduled run prior to calling this method to produce a report.
   * Also have to synchronise all potential services creating report to ensure only one report
   * created per scheduled run so need a latch for this purpose.
   *
   * @return boolean whether or not report has been created successfully.
   */
  private boolean createReport() {
    boolean result = false;
    actionExportLatchManager.setCountDownLatch(
        DISTRIBUTED_OBJECT_KEY_REPORT_LATCH,
        actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT));
    if (!actionExportLockManager.isLocked(DISTRIBUTED_OBJECT_KEY_REPORT)) {
      if (actionExportLockManager.lock(DISTRIBUTED_OBJECT_KEY_REPORT)) {
        result = exportReportService.createReport();
      } else {
        result = true;
      }
    } else {
      result = true;
    }

    try {
      actionExportLatchManager.countDown(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH);
      if (!actionExportLatchManager.awaitCountDownLatch(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH)) {
        log.with(
                "instances_running",
                actionExportInstanceManager.getInstanceCount(DISTRIBUTED_OBJECT_KEY_INSTANCE_COUNT))
            .error("Report run error countdownlatch timed out, should be instances running");
      }
    } catch (InterruptedException e) {
      log.error("Report run error waiting for countdownlatch", e);
    } finally {
      actionExportLockManager.unlock(DISTRIBUTED_OBJECT_KEY_REPORT);
      actionExportLatchManager.deleteCountDownLatch(DISTRIBUTED_OBJECT_KEY_REPORT_LATCH);
    }

    return result;
  }
}
