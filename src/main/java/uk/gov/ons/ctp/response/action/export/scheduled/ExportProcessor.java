package uk.gov.ons.ctp.response.action.export.scheduled;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
import uk.gov.ons.ctp.response.action.export.service.FilenamePrefix;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;

@Component
public class ExportProcessor {
  private static final Logger log = LoggerFactory.getLogger(ExportProcessor.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  private final NotificationFileCreator notificationFileCreator;

  private final ActionRequestRepository actionRequestRepository;

  private ExportJobRepository exportJobRepository;

  public ExportProcessor(
      NotificationFileCreator notificationFileCreator,
      ActionRequestRepository actionRequestRepository,
      ExportJobRepository exportJobRepository) {
    this.notificationFileCreator = notificationFileCreator;
    this.actionRequestRepository = actionRequestRepository;
    this.exportJobRepository = exportJobRepository;
  }

  private class ExportData {
    private List<ActionRequestInstruction> ariList;

    public ExportData(ActionRequestInstruction ari) {
      this.ariList = new ArrayList<ActionRequestInstruction>(Arrays.asList(ari));
    }

    public List<ActionRequestInstruction> getActionRequestInstructionList() {
      return ariList;
    }

    public void addActionRequestInstruction(ActionRequestInstruction ari) {
      if (ariList != null) {
        this.ariList.add(ari);
      } else {
        this.ariList = Arrays.asList(ari);
      }
    }
  }

  @Transactional
  public void processExport() {
    log.info("export process started");
    if (!actionRequestRepository.existsByExportJobIdIsNull()) {
      log.info("nothing to export");
      return;
    }

    ExportJob exportJob = new ExportJob();
    exportJob = exportJobRepository.saveAndFlush(exportJob);

    actionRequestRepository.updateActionsWithExportJob(exportJob.getId());

    Map<String, ExportData> filenamePrefixToDataMap = prepareData(exportJob);

    createAndSendFiles(filenamePrefixToDataMap, exportJob);
    log.info("export process finished");
  }

  private Map<String, ExportData> prepareData(ExportJob exportJob) {
    Stream<ActionRequestInstruction> actionRequestInstructions =
        actionRequestRepository.findByExportJobId(exportJob.getId());

    Map<String, ExportData> filenamePrefixToDataMap = new HashMap<>();

    actionRequestInstructions.forEach(
        ari -> {
          String filenamePrefix =
              FilenamePrefix.getPrefix(ari.getActionType())
                  + "_"
                  + ari.getSurveyRef()
                  + "_"
                  + getExerciseRefWithoutSurveyRef(ari.getExerciseRef());

          if (filenamePrefixToDataMap.containsKey(filenamePrefix)) {
            filenamePrefixToDataMap.get(filenamePrefix).addActionRequestInstruction(ari);
          } else {
            filenamePrefixToDataMap.put(filenamePrefix, new ExportData(ari));
          }
        });

    return filenamePrefixToDataMap;
  }

  private void createAndSendFiles(
      Map<String, ExportData> filenamePrefixToDataMap, ExportJob exportJob) {

    filenamePrefixToDataMap.forEach(
        (filenamePrefix, data) -> {
          List<ActionRequestInstruction> actionRequestInstructions =
              data.getActionRequestInstructionList();
          notificationFileCreator.uploadData(filenamePrefix, actionRequestInstructions, exportJob);
        });
  }

  private String getExerciseRefWithoutSurveyRef(String exerciseRef) {
    String exerciseRefWithoutSurveyRef = StringUtils.substringAfter(exerciseRef, "_");
    return StringUtils.defaultIfEmpty(exerciseRefWithoutSurveyRef, exerciseRef);
  }
}
