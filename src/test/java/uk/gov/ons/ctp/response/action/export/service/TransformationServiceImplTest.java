package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;

/** To unit test TransformationServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class TransformationServiceImplTest {
  @Mock private TemplateMappingService templateMappingService;
  @Mock private TemplateService templateService;

  @InjectMocks private TransformationService transformationService;

  @Test
  public void testProcessActionRequestsNothingToProcess() {
    ExportMessage sftpMessage = transformationService.processActionRequests(new ArrayList<>());
    assertNotNull(sftpMessage);
    assertTrue(sftpMessage.getOutputStreams().isEmpty());
    assertTrue(sftpMessage.getActionRequestIds().isEmpty());
  }

  @Test
  public void testProcessActionRequest() throws CTPException {
    // Given
    ByteArrayOutputStream mockStream = new ByteArrayOutputStream();
    ActionRequestInstruction instruction = new ActionRequestInstruction();
    instruction.setActionType("BSNOT");
    instruction.setActionId(UUID.randomUUID());
    List<ActionRequestInstruction> allActionRequest = Collections.singletonList(instruction);
    TemplateMapping mapping = new TemplateMapping();
    mapping.setTemplate("filename");
    given(templateService.stream(allActionRequest, mapping.getTemplate())).willReturn(mockStream);
    given(templateMappingService.retrieveAllTemplateMappingsByActionType())
        .willReturn(Collections.singletonMap("BSNOT", mapping));

    // When
    ExportMessage sftpMessage = transformationService.processActionRequests(allActionRequest);

    // Then
    assertThat(sftpMessage.getActionRequestIds())
        .containsValues(Collections.singletonList(instruction.getActionId()));
    assertThat(sftpMessage.getOutputStreams()).containsValues(mockStream);
  }

  @Test
  public void testProcessActionRequestTemplateMappingDoesNotExist() {
    // Given
    given(templateMappingService.retrieveAllTemplateMappings())
        .willReturn(Collections.singletonList(new TemplateMapping()));
    ActionRequestInstruction instruction = new ActionRequestInstruction();
    instruction.setActionType("BSNOT");
    instruction.setActionrequestPK(1);

    // When
    ExportMessage sftpMessage =
        transformationService.processActionRequests(Collections.singletonList(instruction));

    // Then
    assertTrue(sftpMessage.isEmpty());
  }
}
