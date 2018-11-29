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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
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

  @Transactional
  public void processExport() {
    if (!actionRequestRepository.existsByExportJobIdIsNull()) {
      return;
    }

    ExportJob exportJob = new ExportJob();
    exportJob = exportJobRepository.saveAndFlush(exportJob);

    actionRequestRepository.updateActionsWithExportJob(exportJob.getId());

    Map<String, Map<String, List<ActionRequestInstruction>>> filenamePrefixToDataMap =
        prepareData(exportJob);

    createAndSendFiles(filenamePrefixToDataMap, exportJob);
  }

  private Map<String, Map<String, List<ActionRequestInstruction>>> prepareData(
      ExportJob exportJob) {
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

    return filenamePrefixToDataMap;
  }

  private void createAndSendFiles(
      Map<String, Map<String, List<ActionRequestInstruction>>> filenamePrefixToDataMap,
      ExportJob exportJob) {

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
