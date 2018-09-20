package uk.gov.ons.ctp.response.action.export.message;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportReportRepository;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportInfo;

@RunWith(MockitoJUnitRunner.class)
public class SftpServicePublisherTest {
  @Mock private ActionRequestRepository actionRequestRepository;

  @Mock private ExportReportRepository exportReportRepository;

  @Mock private ExportInfo exportInfo;

  @Mock private ActionFeedbackPublisher actionFeedbackPubl;

  @InjectMocks SftpServicePublisher underTest;

  @Test
  public void sftpSuccessProcess() {
    UUID actionId = UUID.randomUUID();
    List<String> actionIds = Collections.singletonList(actionId.toString());
    Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("list_actionIds", actionIds);
    MessageHeaders messageHeaders = new MessageHeaders(headerMap);
    byte[] someDummyData = "blablabla".getBytes();
    GenericMessage<byte[]> payload = new GenericMessage(someDummyData, messageHeaders);
    GenericMessage<GenericMessage<byte[]>> message = new GenericMessage(payload);

    // Given
    when(actionRequestRepository.updateDateSentAndSendStateByActionId(any(), any(), any()))
        .thenReturn(1);
    when(actionRequestRepository.retrieveResponseRequiredByActionId(any()))
        .thenReturn(Collections.singletonList(actionId));

    // When
    underTest.sftpSuccessProcess(message);

    // Then
    verify(actionRequestRepository).updateDateSentAndSendStateByActionId(any(), any(), any());
    verify(actionRequestRepository).retrieveResponseRequiredByActionId(any());
    verify(actionFeedbackPubl).sendActionFeedback(any());
    verify(exportInfo).addOutcome(any());
  }

  @Test
  public void sftpFailedProcess() {
    UUID actionId = UUID.randomUUID();
    List<String> actionIds = Collections.singletonList(actionId.toString());
    Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("list_actionIds", actionIds);
    headerMap.put(FileHeaders.REMOTE_FILE, "DUMMY_FILENAME.abc");
    MessageHeaders messageHeaders = new MessageHeaders(headerMap);
    byte[] someDummyData = "blablabla".getBytes();
    GenericMessage<byte[]> payload = new GenericMessage(someDummyData);
    GenericMessage<GenericMessage<byte[]>> message = new GenericMessage(payload, messageHeaders);
    MessagingException messagingException = new MessagingException(message);
    ErrorMessage errorMessage = new ErrorMessage(messagingException);

    // Given
    when(actionRequestRepository.updateSendStateByActionId(any(), any())).thenReturn(1);

    // When
    underTest.sftpFailedProcess(errorMessage);

    // Then
    verify(actionRequestRepository).updateSendStateByActionId(any(), any());
    verify(exportInfo).addOutcome(any());
    verify(exportReportRepository).save(any(ExportReport.class));
  }
}
