package uk.gov.ons.ctp.response.action.export.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportInfo;

@RunWith(MockitoJUnitRunner.class)
public class SftpServicePublisherTest {
  @Mock private ExportFileRepository exportFileRepository;

  @Mock private ExportInfo exportInfo;

  @Mock private ActionFeedbackPublisher actionFeedbackPubl;

  @InjectMocks SftpServicePublisher underTest;

  @Test
  public void sftpSuccessProcess() {
    UUID actionId = UUID.randomUUID();
    String[] responseRequiredIds = {actionId.toString()};
    Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("response_required_id_list", responseRequiredIds);
    headerMap.put("action_count", "999");
    headerMap.put(FileHeaders.REMOTE_FILE, "DUMMY_FILENAME.abc");
    MessageHeaders messageHeaders = new MessageHeaders(headerMap);
    byte[] someDummyData = "blablabla".getBytes();
    GenericMessage<byte[]> payload = new GenericMessage(someDummyData, messageHeaders);
    GenericMessage<GenericMessage<byte[]>> message = new GenericMessage(payload);
    ExportFile exportFile = new ExportFile();

    // Given
    when(exportFileRepository.findOneByFilename(any())).thenReturn(exportFile);

    // When
    underTest.sftpSuccessProcess(message);

    // Then
    verify(exportFileRepository).saveAndFlush(eq(exportFile));
    assertThat(exportFile.getStatus()).isEqualTo(SendStatus.SUCCEEDED);
    assertThat(exportFile.getDateSuccessfullySent()).isNotNull();
    verify(actionFeedbackPubl).sendActionFeedback(any());
    verify(exportInfo).addOutcome(eq("DUMMY_FILENAME.abc transferred with 999 requests."));
  }

  @Test
  public void sftpFailedProcess() {
    UUID actionId = UUID.randomUUID();
    String[] responseRequiredIds = {actionId.toString()};
    Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("response_required_id_list", responseRequiredIds);
    headerMap.put("action_count", "999");
    headerMap.put(FileHeaders.REMOTE_FILE, "DUMMY_FILENAME.abc");
    MessageHeaders messageHeaders = new MessageHeaders(headerMap);
    byte[] someDummyData = "blablabla".getBytes();
    GenericMessage<byte[]> payload = new GenericMessage(someDummyData);
    GenericMessage<GenericMessage<byte[]>> message = new GenericMessage(payload, messageHeaders);
    MessagingException messagingException = new MessagingException(message);
    ErrorMessage errorMessage = new ErrorMessage(messagingException);
    ExportFile exportFile = new ExportFile();

    // Given
    when(exportFileRepository.findOneByFilename(any())).thenReturn(exportFile);

    // When
    underTest.sftpFailedProcess(errorMessage);

    // Then
    verify(exportFileRepository).saveAndFlush(eq(exportFile));
    assertThat(exportFile.getStatus()).isEqualTo(SendStatus.FAILED);
    assertThat(exportFile.getDateSuccessfullySent()).isNull();
    verify(exportInfo).addOutcome(eq("DUMMY_FILENAME.abc transfer failed for 999 requests."));
  }
}
