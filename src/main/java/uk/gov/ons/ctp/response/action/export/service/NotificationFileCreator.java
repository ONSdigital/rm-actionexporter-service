package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.List;
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

    for (ActionRequestInstruction actionRequestInstruction : requests) {
      actionRequestInstruction.setSendState(SendState.SENT);
      actionRequestRepository.save(actionRequestInstruction);
    }

    return transformationService.processActionRequests(requests);
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
