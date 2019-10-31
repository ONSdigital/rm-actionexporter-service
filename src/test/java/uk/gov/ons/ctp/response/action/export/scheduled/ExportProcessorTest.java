package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

@RunWith(MockitoJUnitRunner.class)
public class ExportProcessorTest {
  @Mock private TemplateMappingService templateMappingService;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private NotificationFileCreator notificationFileCreator;
  @Mock private ExportJobRepository exportJobRepository;
  @Mock private TemplateService templateService;

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

    TemplateMapping templateMapping = new TemplateMapping();
    templateMapping.setTemplate("TEMPLATENAME");
    templateMapping.setActionType(actionType);
    templateMapping.setFileNamePrefix("FILENAMEPREFIX");

    Map<String, TemplateMapping> fileNameTemplateMappings = new HashMap<>();
    fileNameTemplateMappings.put("ACTIONTYPE", templateMapping);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] someBytes = "HEREARESOMEBYTES".getBytes();
    bos.write(someBytes, someBytes.length, 0);

    // Given
    given(actionRequestRepository.existsByExportJobIdIsNull()).willReturn(true);
    given(exportJobRepository.saveAndFlush(any())).willReturn(exportJob);
    given(actionRequestRepository.findByExportJobId(any()))
        .willReturn(Collections.singletonList(ari).stream());
    given(templateMappingService.retrieveAllTemplateMappingsByActionType())
        .willReturn(fileNameTemplateMappings);
    given(templateService.stream(any(), any())).willReturn(bos);

    // When
    exportProcessor.processExport();

    // Verify
    verify(exportJobRepository).saveAndFlush(any());
    verify(actionRequestRepository).updateActionsWithExportJob(eq(exportJob.getId()));
    verify(actionRequestRepository).findByExportJobId(eq(exportJob.getId()));
    verify(templateMappingService).retrieveAllTemplateMappingsByActionType();

    ArgumentCaptor<ByteArrayOutputStream> bosCaptor =
        ArgumentCaptor.forClass(ByteArrayOutputStream.class);
    String[] responsesRequired = {ari.getActionId().toString()};
    verify(notificationFileCreator)
        .uploadData(
            eq("FILENAMEPREFIX_SURVEYREF_EXERCISEREF"),
            bosCaptor.capture(),
            eq(exportJob),
            eq(responsesRequired),
            eq(1));
    assertThat(bosCaptor.getValue().toByteArray()).isEqualTo(bos.toByteArray());
  }
}
