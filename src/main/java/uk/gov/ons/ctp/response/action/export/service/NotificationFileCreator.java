package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.domain.SendState;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;

@Service
public class NotificationFileCreator {

  private static final Logger log = LoggerFactory.getLogger(NotificationFileCreator.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  private static final int BATCH_SIZE = 10000;

  private final ActionRequestRepository actionRequestRepository;

  private final TransformationService transformationService;

  private final SftpServicePublisher sftpService;

  private final EventPublisher eventPublisher;

  private final Clock clock;

  public NotificationFileCreator(
      ActionRequestRepository actionRequestRepository,
      TransformationService transformationService,
      SftpServicePublisher sftpService,
      EventPublisher eventPublisher,
      Clock clock) {
    this.actionRequestRepository = actionRequestRepository;
    this.transformationService = transformationService;
    this.sftpService = sftpService;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  public void publishNotificationFile(
      SurveyRefExerciseRef surveyRefExerciseRef,
      List<TemplateMapping> templateMappings,
      String filenamePrefix) {
    ExportMessage dataForFile = createDataForFile(surveyRefExerciseRef, templateMappings);
    uploadFile(dataForFile, filenamePrefix);
  }

  private ExportMessage createDataForFile(
      final SurveyRefExerciseRef surveyRefExerciseRef,
      final List<TemplateMapping> templateMappings) {
    return templateMappings
        .stream()
        .map(templateMapping -> createExportData(surveyRefExerciseRef, templateMapping))
        .reduce(ExportMessage::merge)
        .orElse(new ExportMessage());
  }

  private ExportMessage createExportData(
      SurveyRefExerciseRef surveyRefExerciseRefTuple, TemplateMapping templateMapping) {
    List<ActionRequestInstruction> requests =
        actionRequestRepository.findByActionTypeAndExerciseRefAndSurveyRefAndSendState(
            templateMapping.getActionType(),
            surveyRefExerciseRefTuple.getExerciseRef(),
            surveyRefExerciseRefTuple.getSurveyRef(),
            SendState.INIT);

    ExportMessage exportMessage = transformationService.processActionRequests(requests);

    List<List<ActionRequestInstruction>> subLists = Lists.partition(requests, BATCH_SIZE);
    Set<UUID> actionIds = new HashSet<>();
    subLists.forEach(
        (batch) -> {
          batch.forEach(
              (request) -> {
                actionIds.add(request.getActionId());
              });
          int saved =
              actionRequestRepository.updateSendStateByActionId(actionIds, SendState.QUEUED);

          if (actionIds.size() == saved) {
            log.with("action_ids", actionIds).error("ActionRequests failed to mark as QUEUED");
          }
        });

    return exportMessage;
  }

  private void uploadFile(ExportMessage exportData, String filenamePrefix) {
    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String filename = String.format("%s_%s.csv", filenamePrefix, now);
    if (exportData.isEmpty()) {
      log.with("filename", filename).info("No data to generate file");
      return;
    }
    log.with("filename", filename).info("Uploading file");
    sftpService.sendMessage(
        filename,
        exportData.getMergedActionRequestIdsAsStrings(),
        exportData.getMergedOutputStreams());
    eventPublisher.publishEvent("Printed file " + filename);
  }
}
