package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

@RunWith(MockitoJUnitRunner.class)
public class ExportProcessorTest {
  @Mock private TemplateMappingService templateMappingService;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private NotificationFileCreator notificationFileCreator;
  @Mock private ExportJobRepository exportJobRepository;

  @InjectMocks private ExportProcessor exportProcessor;

  @Test
  public void testHappyPath() {
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String actionType = "ACTIONTYPE";

    ActionRequestInstruction ari = new ActionRequestInstruction();
    ari.setActionId(UUID.randomUUID());
    ari.setActionType(actionType);
    ari.setSurveyRef("SURVEYREF");
    ari.setExerciseRef("EXERCISEREF");
    ari.setResponseRequired(true);

    List<ActionRequestInstruction> actionRequestInstructions = Collections.singletonList(ari);

    TemplateMapping templateMapping = new TemplateMapping();
    templateMapping.setTemplate("TEMPLATENAME");
    templateMapping.setActionType(actionType);
    templateMapping.setFileNamePrefix("FILENAMEPREFIX");

    Map<String, TemplateMapping> fileNameTemplateMappings = new HashMap<>();
    fileNameTemplateMappings.put("ACTIONTYPE", templateMapping);

    // Given
    given(actionRequestRepository.existsByExportJobIdIsNull()).willReturn(true);
    given(exportJobRepository.saveAndFlush(any())).willReturn(exportJob);

    given(actionRequestRepository.findByExportJobId(any()))
        .willReturn(actionRequestInstructions.stream());
    given(templateMappingService.retrieveAllTemplateMappingsByActionType())
        .willReturn(fileNameTemplateMappings);

    // When
    exportProcessor.processExport();

    // Verify
    verify(exportJobRepository).saveAndFlush(any());
    verify(actionRequestRepository).updateActionsWithExportJob(eq(exportJob.getId()));
    verify(actionRequestRepository).findByExportJobId(eq(exportJob.getId()));
    verify(templateMappingService).retrieveAllTemplateMappingsByActionType();

    verify(notificationFileCreator)
        .uploadData(
            eq("FILENAMEPREFIX_SURVEYREF_EXERCISEREF"),
            eq(actionRequestInstructions),
            eq(exportJob));
  }
}
