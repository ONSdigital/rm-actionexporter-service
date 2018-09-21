package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob.JobStatus;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

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

  private TemplateService templateService;

  private ExportJobRepository exportJobRepository;

  public ExportScheduler(
      TemplateMappingService templateMappingService,
      NotificationFileCreator notificationFileCreator,
      ActionRequestRepository actionRequestRepository,
      RedissonClient redissonClient,
      AppConfig appConfig,
      TemplateService templateService,
      ExportJobRepository exportJobRepository) {
    this.templateMappingService = templateMappingService;
    this.notificationFileCreator = notificationFileCreator;
    this.actionRequestRepository = actionRequestRepository;
    this.redissonClient = redissonClient;
    this.appConfig = appConfig;
    this.templateService = templateService;
    this.exportJobRepository = exportJobRepository;
  }

  /** Carry out scheduled actions according to configured cron expression */
  @Scheduled(cron = "#{appConfig.exportSchedule.cronExpression}")
  @Transactional
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
    ExportJob exportJob = new ExportJob();
    exportJob.setId(UUID.randomUUID());
    exportJob.setStatus(JobStatus.INIT);
    exportJobRepository.saveAndFlush(exportJob);

    actionRequestRepository.updateActionsWithExportJob(exportJob.getId());

    prepareFilesToSend(exportJob);
  }

  private void prepareFilesToSend(ExportJob exportJob) {
    Stream<ActionRequestInstruction> actionRequestInstructions = actionRequestRepository
        .findByExportJobId(exportJob.getId());

    Map<String, TemplateMapping> actionTypeTemplateMappings = templateMappingService
        .retrieveAllTemplateMappingsByActionType();

    Map<String, List<TemplateMapping>> fileNameTemplateMappings = templateMappingService
        .retrieveAllTemplateMappingsByFilename();

    Set<String> filenames = fileNameTemplateMappings.keySet();
    Map<String, List<ActionRequestInstruction>> filenamePrefixToActionRequestInstructionMap =
        new HashMap<>();

    actionRequestInstructions.forEach(ari -> {
      TemplateMapping actionTemplateMapping = actionTypeTemplateMappings.get(ari.getActionType());
      if (actionTemplateMapping == null) {
        log.with("action_request_instruction", ari)
            .warn("No action type template for action request instruction");
      }

      for (String filename : filenames) {
        List<TemplateMapping> templateMappings = fileNameTemplateMappings.get(filename);
        for (TemplateMapping templateMapping : templateMappings) {
          if (templateMapping.getActionType().equals(ari.getActionType())) {
            final String filenamePrefix =
                filename
                    + "_"
                    + ari.getSurveyRef()
                    + "_"
                    + ari.getExerciseRef();

            List<ActionRequestInstruction> dataForSurveyRefExerciseRef =
                filenamePrefixToActionRequestInstructionMap
                    .computeIfAbsent(filenamePrefix, key -> new LinkedList<>());

            dataForSurveyRefExerciseRef.add(ari);
          }
        }
      }
    });

    Map<String, ByteArrayOutputStream> filenamePrefixToStreamsMap = new HashMap<>();
    filenamePrefixToActionRequestInstructionMap.forEach((filenamePrefix, ariSubset) -> {
      ByteArrayOutputStream stream = templateService.stream(ariSubset, filenamePrefix);
      filenamePrefixToStreamsMap.put(filenamePrefix, stream);
    });

    sendFiles(filenamePrefixToStreamsMap, exportJob);
  }

  private void sendFiles(Map<String, ByteArrayOutputStream> filenamePrefixToStreamsMap,
      ExportJob exportJob) {
    filenamePrefixToStreamsMap.forEach((filenamePrefix, data) -> notificationFileCreator
        .uploadData(filenamePrefix, data, exportJob));

    exportJob.setStatus(JobStatus.QUEUED);
    exportJobRepository.saveAndFlush(exportJob);
  }
}
