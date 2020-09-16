package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeleteProcessorTest {
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private ExportJobRepository exportJobRepository;
  @Mock private ExportFileRepository exportFileRepository;

  @InjectMocks private DeleteProcessor deleteProcessor;

  private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

  @Test
  public void testHappyPath() {
    ExportJob exportJob = new ExportJob(UUID.randomUUID());
    String actionType = "ACTIONTYPE";

    ExportFile exportFile = new ExportFile();
    exportFile.setId(UUID.randomUUID());
    exportFile.setFilename("BSNOT_009_2011_14092020_1000.csv");
    exportFile.setExportJobId(exportJob.getId());
    exportFile.setDateSuccessfullySent(new Timestamp(System.currentTimeMillis() - (90 * DAY_IN_MS)));
    exportFile.setStatus(ExportFile.SendStatus.QUEUED);
    exportFile.setRowCount(1);

    ActionRequestInstruction ari = new ActionRequestInstruction();
    ari.setActionId(UUID.randomUUID());
    ari.setActionType(actionType);
    ari.setSurveyRef("SURVEYREF");
    ari.setExerciseRef("EXERCISEREF");
    ari.setResponseRequired(true);
    ari.setExportJobId(exportJob.getId());

    List<ExportJob> exportJobList = Collections.singletonList(exportJob);
    List<ExportFile> exportFileList = Collections.singletonList(exportFile);

    // Given
    given(exportJobRepository.findAll()).willReturn(exportJobList);
    given(exportFileRepository.findAllByExportJobId(exportJob.getId())).willReturn(exportFileList);
    given(actionRequestRepository.findByExportJobId(any())).willReturn(Stream.of(ari));

    // When
    deleteProcessor.triggerDelete();

    // Verify
    verify(exportJobRepository).findAll();
    verify(exportFileRepository).findAllByExportJobId(eq(exportJob.getId()));
    verify(actionRequestRepository).findByExportJobId(eq(exportJob.getId()));
    verify(exportFileRepository).delete(exportFile);
  }
}
