package uk.gov.ons.ctp.response.action.export.scheduled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

@Component
public class ExportProcessor {
  private static final Logger log = LoggerFactory.getLogger(ExportProcessor.class);

  private final TemplateMappingService templateMappingService;

  private final NotificationFileCreator notificationFileCreator;

  private final ActionRequestRepository actionRequestRepository;

  private TemplateService templateService;

  private ExportJobRepository exportJobRepository;

  public ExportProcessor(
      TemplateMappingService templateMappingService,
      NotificationFileCreator notificationFileCreator,
      ActionRequestRepository actionRequestRepository,
      TemplateService templateService,
      ExportJobRepository exportJobRepository) {
    this.templateMappingService = templateMappingService;
    this.notificationFileCreator = notificationFileCreator;
    this.actionRequestRepository = actionRequestRepository;
    this.templateService = templateService;
    this.exportJobRepository = exportJobRepository;
  }

  private class ExportData {
    private TemplateMapping mapping;
    private List<ActionRequestInstruction> ariList;

    public ExportData(TemplateMapping mapping, ActionRequestInstruction ari) {
      this.mapping = mapping;
      this.ariList = new ArrayList<ActionRequestInstruction>(Arrays.asList(ari));
    }

    public TemplateMapping getTemplateMapping() {
      return mapping;
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
    if (!actionRequestRepository.existsByExportJobIdIsNull()) {
      return;
    }

    ExportJob exportJob = new ExportJob();
    exportJob = exportJobRepository.saveAndFlush(exportJob);

    actionRequestRepository.updateActionsWithExportJob(exportJob.getId());

    Map<String, ExportData> filenamePrefixToDataMap = prepareData(exportJob);

    createAndSendFiles(filenamePrefixToDataMap, exportJob);
  }

  private Map<String, ExportData> prepareData(ExportJob exportJob) {
    Stream<ActionRequestInstruction> actionRequestInstructions =
        actionRequestRepository.findByExportJobId(exportJob.getId());

    Map<String, TemplateMapping> templateMappings =
        templateMappingService.retrieveAllTemplateMappingsByActionType();

    Map<String, ExportData> filenamePrefixToDataMap = new HashMap<>();

    actionRequestInstructions.forEach(
        ari -> {
          if (templateMappings.containsKey(ari.getActionType())) {
            TemplateMapping mapping = templateMappings.get(ari.getActionType());
            String filenamePrefix =
                mapping.getFileNamePrefix()
                    + "_"
                    + ari.getSurveyRef()
                    + "_"
                    + getExerciseRefWithoutSurveyRef(ari.getExerciseRef());

            if (filenamePrefixToDataMap.containsKey(filenamePrefix)) {
              filenamePrefixToDataMap.get(filenamePrefix).addActionRequestInstruction(ari);
            } else {
              filenamePrefixToDataMap.put(filenamePrefix, new ExportData(mapping, ari));
            }
          }
        });

    return filenamePrefixToDataMap;
  }

  private void createAndSendFiles(
      Map<String, ExportData> filenamePrefixToDataMap, ExportJob exportJob) {

    filenamePrefixToDataMap.forEach(
        (filenamePrefix, data) -> {
          List<ByteArrayOutputStream> streamList = new LinkedList<>();

          streamList.add(
              templateService.stream(
                  data.getActionRequestInstructionList(), data.getTemplateMapping().getTemplate()));

          Set<String> responseRequiredList =
              data.getActionRequestInstructionList().stream()
                  .filter(ActionRequestInstruction::isResponseRequired)
                  .map(ari -> ari.getActionId().toString())
                  .collect(Collectors.toSet());

          notificationFileCreator.uploadData(
              filenamePrefix,
              getMergedStreams(streamList),
              exportJob,
              responseRequiredList.toArray(new String[0]),
              data.getActionRequestInstructionList().size());
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
