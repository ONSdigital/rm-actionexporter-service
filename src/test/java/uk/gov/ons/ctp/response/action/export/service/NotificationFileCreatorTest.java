package uk.gov.ons.ctp.response.action.export.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationFileCreatorTest {

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  @Mock private Clock clock;
  @Mock private ExportFileRepository exportFileRepository;
  @Mock private AppConfig appConfig;
  @Mock private PrintFileService printFileService;
  @InjectMocks private NotificationFileCreator notificationFileCreator;

  @Test
  public void shouldCreateTheCorrectFilename() {
    String actionType = "ACTIONTYPE";
    ActionRequestInstruction ari = new ActionRequestInstruction();
    ari.setActionId(UUID.randomUUID());
    ari.setActionType(actionType);
    ari.setSurveyRef("SURVEYREF");
    ari.setExerciseRef("EXERCISEREF");
    ari.setResponseRequired(true);

    List<ActionRequestInstruction> actionRequestInstructions = Collections.singletonList(ari);

    ExportJob exportJob = new ExportJob(UUID.randomUUID());

    Date now = new Date();

    // Given
    given(clock.millis()).willReturn(now.getTime());

    // When
    notificationFileCreator.uploadData("TESTFILENAMEPREFIX", actionRequestInstructions, exportJob);

    // Then
    String expectedFilename =
        String.format("TESTFILENAMEPREFIX_%s.csv", FILENAME_DATE_FORMAT.format(now));
    ArgumentCaptor<ExportFile> exportFileArgumentCaptor = ArgumentCaptor.forClass(ExportFile.class);
    verify(exportFileRepository).saveAndFlush(exportFileArgumentCaptor.capture());
    assertThat(exportFileArgumentCaptor.getValue().getFilename()).isEqualTo(expectedFilename);
    assertThat(exportFileArgumentCaptor.getValue().getExportJobId()).isEqualTo(exportJob.getId());
    assertThat(exportFileArgumentCaptor.getValue().getStatus()).isEqualTo(SendStatus.QUEUED);

    verify(printFileService).send(expectedFilename, actionRequestInstructions);
  }

  @Test
  public void shouldThrowExceptionForDuplicateFilename() {
    String actionType = "ACTIONTYPE";
    ActionRequestInstruction ari = new ActionRequestInstruction();
    ari.setActionId(UUID.randomUUID());
    ari.setActionType(actionType);
    ari.setSurveyRef("SURVEYREF");
    ari.setExerciseRef("EXERCISEREF");
    ari.setResponseRequired(true);

    List<ActionRequestInstruction> actionRequestInstructions = Collections.singletonList(ari);
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    Date now = new Date();
    boolean expectedExceptionThrown = false;

    // Given
    given(clock.millis()).willReturn(now.getTime());
    given(exportFileRepository.existsByFilename(any())).willReturn(true);

    // When
    try {
      notificationFileCreator.uploadData(
          "TESTFILENAMEPREFIX", actionRequestInstructions, exportJob);
    } catch (RuntimeException ex) {
      expectedExceptionThrown = true;
    }

    // Then
    assertThat(expectedExceptionThrown).isTrue();
    verify(exportFileRepository, never()).saveAndFlush(any());
  }
}
