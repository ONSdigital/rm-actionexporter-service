package uk.gov.ons.ctp.response.action.export.scheduled;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
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

  private static final String ACTION_EXECUTION_LOCK = "actionexport.request.execution";

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
    if (!actionRequestRepository.existsByExportJobIdIsNull()) {
      return;
    }

    ExportJob exportJob = new ExportJob();
    exportJob = exportJobRepository.saveAndFlush(exportJob);

    actionRequestRepository.updateActionsWithExportJob(exportJob.getId());

    prepareAndSendFiles(exportJob);
  }

  private void prepareAndSendFiles(ExportJob exportJob) {
    Stream<ActionRequestInstruction> actionRequestInstructions =
        actionRequestRepository.findByExportJobId(exportJob.getId());

    Map<String, List<TemplateMapping>> fileNameTemplateMappings =
        templateMappingService.retrieveAllTemplateMappingsByFilename();

    Set<String> filenames = fileNameTemplateMappings.keySet();
    Map<String, Map<String, List<ActionRequestInstruction>>> filenamePrefixToDataMap =
        new HashMap<>();

    actionRequestInstructions.forEach(
        ari -> {
          for (String filename : filenames) {
            List<TemplateMapping> templateMappings = fileNameTemplateMappings.get(filename);
            for (TemplateMapping templateMapping : templateMappings) {
              if (templateMapping.getActionType().equals(ari.getActionType())) {
                String filenamePrefix =
                    filename
                        + "_"
                        + ari.getSurveyRef()
                        + "_"
                        + getExerciseRefWithoutSurveyRef(ari.getExerciseRef());

                Map<String, List<ActionRequestInstruction>> templateNameMap =
                    filenamePrefixToDataMap.computeIfAbsent(filenamePrefix, key -> new HashMap<>());

                List<ActionRequestInstruction> ariSubset =
                    templateNameMap.computeIfAbsent(
                        templateMapping.getTemplate(), key -> new LinkedList<>());

                ariSubset.add(ari);
              }
            }
          }
        });

    filenamePrefixToDataMap.forEach(
        (filenamePrefix, data) -> {
          List<ByteArrayOutputStream> streamList = new LinkedList<>();
          Set<String> responseRequiredList = new HashSet<>();
          AtomicInteger actionCount = new AtomicInteger(0);

          data.forEach(
              (templateName, actionRequestList) -> {
                streamList.add(templateService.stream(actionRequestList, templateName));
                actionRequestList.forEach(
                    ari -> {
                      actionCount.incrementAndGet();

                      if (ari.isResponseRequired()) {
                        responseRequiredList.add(ari.getActionId().toString());
                      }
                    });
              });

          notificationFileCreator.uploadData(
              filenamePrefix,
              getMergedStreams(streamList),
              exportJob,
              responseRequiredList.toArray(new String[0]),
              actionCount.get());
        });
  }

  private ByteArrayOutputStream getMergedStreams(List<ByteArrayOutputStream> streamList) {
    ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();

    for (ByteArrayOutputStream outputStream : streamList) {
      try {
        mergedStream.write(outputStream.toByteArray());
      } catch (IOException ex) {
        log.error("Error merging ByteArrayOutputStreams", ex);
        throw new RuntimeException();
      }
    }

    return mergedStream;
  }

  private String getExerciseRefWithoutSurveyRef(String exerciseRef) {
    String exerciseRefWithoutSurveyRef = StringUtils.substringAfter(exerciseRef, "_");
    return StringUtils.defaultIfEmpty(exerciseRefWithoutSurveyRef, exerciseRef);
  }
}
