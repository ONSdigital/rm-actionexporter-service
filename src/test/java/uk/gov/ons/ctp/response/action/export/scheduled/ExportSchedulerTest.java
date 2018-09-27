package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.config.DataGrid;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

@RunWith(MockitoJUnitRunner.class)
public class ExportSchedulerTest {

  @Mock private RedissonClient redissonClient;
  @Mock private TemplateMappingService templateMappingService;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private NotificationFileCreator notificationFileCreator;
  @Mock private AppConfig appConfig;
  @Mock private ExportJobRepository exportJobRepository;
  @Mock private TemplateService templateService;
  @InjectMocks private ExportScheduler exportScheduler;

  @Captor private ArgumentCaptor<List<ActionRequestInstruction>> ariListCaptor;

  @Test
  public void shouldLockAndUnlock() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);
    given(actionRequestRepository.existsByExportJobIdIsNull()).willReturn(false);

    // When
    exportScheduler.scheduleExport();

    // Verify
    InOrder inOrder = inOrder(mockLock, actionRequestRepository);
    inOrder.verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    inOrder.verify(actionRequestRepository).existsByExportJobIdIsNull();
    inOrder.verify(mockLock).unlock();
  }

  @Test
  public void shouldUnlockWhenExceptionThrown() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);
    given(actionRequestRepository.existsByExportJobIdIsNull()).willThrow(RuntimeException.class);

    try {
      // When
      exportScheduler.scheduleExport();
    } catch (Exception ex) {
      // Ignored
    }

    // Verify
    InOrder inOrder = inOrder(mockLock, actionRequestRepository);
    inOrder.verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    inOrder.verify(actionRequestRepository).existsByExportJobIdIsNull();
    inOrder.verify(mockLock).unlock();
  }

  @Test
  public void testHappyPath() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);
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

    Map<String, List<TemplateMapping>> fileNameTemplateMappings = new HashMap<>();
    fileNameTemplateMappings.put("FILENAMEPREFIX", Collections.singletonList(templateMapping));

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] someBytes = "HEREARESOMEBYTES".getBytes();
    bos.write(someBytes, someBytes.length, 0);

    // Given
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);
    given(actionRequestRepository.existsByExportJobIdIsNull()).willReturn(true);
    given(exportJobRepository.saveAndFlush(any())).willReturn(exportJob);
    given(actionRequestRepository.findByExportJobId(any()))
        .willReturn(Collections.singletonList(ari).stream());
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(fileNameTemplateMappings);
    given(templateService.stream(any(), any())).willReturn(bos);

    // When
    exportScheduler.scheduleExport();

    // Verify
    verify(exportJobRepository).saveAndFlush(any());
    verify(actionRequestRepository).updateActionsWithExportJob(eq(exportJob.getId()));
    verify(actionRequestRepository).findByExportJobId(eq(exportJob.getId()));
    verify(templateMappingService).retrieveAllTemplateMappingsByFilename();
    verify(templateService).stream(ariListCaptor.capture(), eq("TEMPLATENAME"));
    assertThat(ariListCaptor.getValue().size()).isEqualTo(1);
    assertThat(ariListCaptor.getValue().get(0)).isEqualTo(ari);

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
