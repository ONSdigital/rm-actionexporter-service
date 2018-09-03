package uk.gov.ons.ctp.response.action.export.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.action.export.ByteArraySteamHelper.baosWithData;
import static uk.gov.ons.ctp.response.action.export.TemplateMappings.templateMappingsWithActionType;
import static uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator.FILENAME_DATE_FORMAT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;

@RunWith(MockitoJUnitRunner.class)
public class NotificationFileCreatorTest {

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  @Mock private Clock clock;
  @Mock private SftpServicePublisher sftpService;
  @Mock private ActionRequestService actionRequestService;
  @Mock private EventPublisher eventPublisher;
  @Mock private TransformationService transformationService;
  @InjectMocks private NotificationFileCreator notificationFileCreator;

  @Test
  public void shouldCreateNotificationFile() throws IOException {
    // Given
    Date now = new Date();
    given(clock.millis()).willReturn(now.getTime());
    ExportMessage message = new ExportMessage();
    UUID first = UUID.randomUUID();
    ByteArrayOutputStream data = baosWithData("data");
    message.getActionRequestIds().put("BSNOT", Collections.singletonList(first));
    message.getOutputStreams().put("BSNOT", data);
    given(transformationService.processActionRequests(any())).willReturn(message);
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");

    // When
    notificationFileCreator.publishNotificationFile(
        surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "filename_1_1");

    // Then
    List<String> ids = Collections.singletonList(first.toString());
    String filename = String.format("filename_1_1_%s.csv", FILENAME_DATE_FORMAT.format(now));
    verify(sftpService)
        .sendMessage(
            any(), eq(ids), argThat(new ByteArrayOutputStreamMatcher(data.toString())));
  }

  @Test
  public void shouldHaveFilenameInCorrectFormatWithddMMyyyy_HHmmDateTime() throws IOException {
    // Given
    Date now = new Date();
    given(clock.millis()).willReturn(now.getTime());
    ExportMessage message = new ExportMessage();
    UUID first = UUID.randomUUID();
    ByteArrayOutputStream data = baosWithData("data");
    message.getActionRequestIds().put("BSNOT", Collections.singletonList(first));
    message.getOutputStreams().put("BSNOT", data);
    given(transformationService.processActionRequests(any())).willReturn(message);
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");

    // When
    notificationFileCreator.publishNotificationFile(
        surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "filename_1_1");

    // Then
    List<String> ids = Collections.singletonList(first.toString());
    String filename = String.format("filename_1_1_%s.csv", FILENAME_DATE_FORMAT.format(now));
    verify(sftpService)
        .sendMessage(
            eq(filename), eq(ids), argThat(new ByteArrayOutputStreamMatcher(data.toString())));
  }

  @Test
  public void shouldProduceOneFilePerActionType() throws IOException {
    // Given
    Date now = new Date();
    given(clock.millis()).willReturn(now.getTime());
    ExportMessage message = new ExportMessage();
    UUID first = UUID.randomUUID();
    message.getActionRequestIds().put("BSNOT", Collections.singletonList(first));
    message.getOutputStreams().put("BSNOT", new ByteArrayOutputStream());
    UUID second = UUID.randomUUID();
    message.getActionRequestIds().put("BSPRENOT", Collections.singletonList(second));
    given(transformationService.processActionRequests(any())).willReturn(message);
    message.getOutputStreams().put("BSPRENOT", new ByteArrayOutputStream());
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");

    // When
    notificationFileCreator.publishNotificationFile(
        surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "filename_1_1");

    // Then
    List<String> ids = Arrays.asList(first.toString(), second.toString());
    String bsNotFilename = String.format("filename_1_1_%s.csv", FILENAME_DATE_FORMAT.format(now));
    verify(sftpService).sendMessage(eq(bsNotFilename), eq(ids), any(ByteArrayOutputStream.class));
    String bspreNotFilename =
        String.format("filename_1_1_%s.csv", FILENAME_DATE_FORMAT.format(now));
    verify(sftpService)
        .sendMessage(eq(bspreNotFilename), eq(ids), any(ByteArrayOutputStream.class));
  }

  @Test
  public void shouldPublishMessageWhenFileUploaded() throws IOException {
    // Given
    Date now = new Date();
    given(clock.millis()).willReturn(now.getTime());
    ExportMessage message = new ExportMessage();
    message.getActionRequestIds().put("BSNOT", Collections.singletonList(UUID.randomUUID()));
    message.getOutputStreams().put("BSNOT", new ByteArrayOutputStream());
    given(transformationService.processActionRequests(any())).willReturn(message);
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");

    // When
    notificationFileCreator.publishNotificationFile(
        surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "filename_1_1");

    // Then
    verify(eventPublisher).publishEvent("Print file");
  }

  private static class ByteArrayOutputStreamMatcher extends ArgumentMatcher<ByteArrayOutputStream> {

    private String data;

    private ByteArrayOutputStreamMatcher(String data) {
      this.data = data;
    }

    @Override
    public boolean matches(Object o) {
      return data.equals(o.toString());
    }
  }
}
