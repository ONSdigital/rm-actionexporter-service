package uk.gov.ons.ctp.response.action.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.config.GCS;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.message.UploadObjectGCS;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationFileCreatorTest {

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  @Mock private Clock clock;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private SftpServicePublisher sftpService;
  @Mock private EventPublisher eventPublisher;
  @Mock private ExportFileRepository exportFileRepository;
  @Mock private AppConfig appConfig;
  @Mock private UploadObjectGCS uploadObjectGCS;
  @InjectMocks private NotificationFileCreator notificationFileCreator;

  @Test
  public void shouldCreateTheCorrectFilename() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();
    GCS mockGCS = mock(GCS.class);
    mockGCS.setEnabled(false);
    // Given
    given(appConfig.getGcs()).willReturn(mockGCS);
    given(clock.millis()).willReturn(now.getTime());

    // When
    notificationFileCreator.uploadData(
        "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);

    // Then
    String expectedFilename =
        String.format("TESTFILENAMEPREFIX_%s.csv", FILENAME_DATE_FORMAT.format(now));
    ArgumentCaptor<ExportFile> exportFileArgumentCaptor = ArgumentCaptor.forClass(ExportFile.class);
    verify(exportFileRepository).saveAndFlush(exportFileArgumentCaptor.capture());
    assertThat(exportFileArgumentCaptor.getValue().getFilename()).isEqualTo(expectedFilename);
    assertThat(exportFileArgumentCaptor.getValue().getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(exportFileArgumentCaptor.getValue().getStatus()).isEqualTo(SendStatus.QUEUED);

    //    verify(sftpService)
    //        .sendMessage(eq(expectedFilename), eq(responseRequiredList), eq("666"), eq(bos));

    verify(eventPublisher).publishEvent(eq("Printed file " + expectedFilename));
  }

  @Test
  public void shouldCreateTheCorrectFilenameAndUploadDataToGCS() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();
    GCS mockGCS = mock(GCS.class);
    // Given
    given(appConfig.getGcs()).willReturn(mockGCS);
    given(clock.millis()).willReturn(now.getTime());

    // When
    when(appConfig.getGcs().isEnabled()).thenReturn(true);
    when(appConfig.getGcs().getBucket()).thenReturn("testBucket");
    notificationFileCreator.uploadData(
        "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);

    // Then
    String expectedFilename =
        String.format("TESTFILENAMEPREFIX_%s.csv", FILENAME_DATE_FORMAT.format(now));
    ArgumentCaptor<ExportFile> exportFileArgumentCaptor = ArgumentCaptor.forClass(ExportFile.class);
    verify(exportFileRepository).saveAndFlush(exportFileArgumentCaptor.capture());
    assertThat(exportFileArgumentCaptor.getValue().getFilename()).isEqualTo(expectedFilename);
    assertThat(exportFileArgumentCaptor.getValue().getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(exportFileArgumentCaptor.getValue().getStatus()).isEqualTo(SendStatus.QUEUED);

    //    verify(sftpService)
    //        .sendMessage(eq(expectedFilename), eq(responseRequiredList), eq("666"), eq(bos));

    verify(eventPublisher).publishEvent(eq("Printed file " + expectedFilename));
    //    verify(uploadObjectGCS).uploadObject(eq(expectedFilename), eq("testBucket"), eq(bos));
  }

  @Test
  public void shouldThrowExceptionForDuplicateFilename() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String[] responseRequiredList = {"123", "ABC", "FOO", "BAR"};
    Date now = new Date();
    boolean expectedExceptionThrown = false;

    // Given
    given(clock.millis()).willReturn(now.getTime());
    given(exportFileRepository.existsByFilename(any())).willReturn(true);

    // When
    try {
      notificationFileCreator.uploadData(
          "TESTFILENAMEPREFIX", bos, exportJob, responseRequiredList, 666);
    } catch (RuntimeException ex) {
      expectedExceptionThrown = true;
    }

    // Then
    assertThat(expectedExceptionThrown).isTrue();
    verify(exportFileRepository, never()).saveAndFlush(any());
    verify(sftpService, never()).sendMessage(any(), any(), any(), any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
