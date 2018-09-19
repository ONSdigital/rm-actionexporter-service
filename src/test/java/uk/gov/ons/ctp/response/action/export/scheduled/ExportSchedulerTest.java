package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.action.export.TemplateMappings.templateMappingsWithActionType;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.config.DataGrid;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

@RunWith(MockitoJUnitRunner.class)
public class ExportSchedulerTest {

  @Mock private RedissonClient redissonClient;
  @Mock private TemplateMappingService templateMappingService;
  @Mock private ActionRequestRepository actionRequestRepository;
  @Mock private NotificationFileCreator notificationFileCreator;
  @Mock private AppConfig appConfig;
  @InjectMocks private ExportScheduler exportScheduler;

  @Test
  public void shouldLockOnFilenameAndCollectionExercise() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(actionRequestRepository.findDistinctSurveyAndExerciseRefs())
        .willReturn(Collections.singletonList(new SurveyRefExerciseRef("1", "1")));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(Collections.singletonMap("filename", templateMappingsWithActionType("BSNOT")));
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);

    // When
    exportScheduler.scheduleExport();

    // Then
    verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    verify(mockLock).unlock();
  }

  @Test(expected = Exception.class)
  public void shouldUnlockWhenExceptionThrown() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    given(actionRequestRepository.findDistinctSurveyAndExerciseRefs())
        .willReturn(Collections.singletonList(new SurveyRefExerciseRef("1", "1")));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(Collections.singletonMap("filename", templateMappingsWithActionType("BSNOT")));
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);
    doThrow(Exception.class)
        .when(notificationFileCreator)
        .publishNotificationFile(any(), anyListOf(TemplateMapping.class), any());

    // When
    exportScheduler.scheduleExport();

    // Then
    verify(mockLock).tryLock(anyLong(), any(TimeUnit.class));
    verify(mockLock).unlock();
  }

  @Test
  public void shouldFileForAllTemplateMappings() throws InterruptedException {
    RLock mockLock = mock(RLock.class);
    DataGrid mockDataGrid = mock(DataGrid.class);

    // Given
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");
    given(actionRequestRepository.findDistinctSurveyAndExerciseRefs())
        .willReturn(Collections.singletonList(surveyRefExerciseRef));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(
            ImmutableMap.of(
                "first_file",
                templateMappingsWithActionType("BSNOT"),
                "second_file",
                templateMappingsWithActionType("BSPRENOT")));
    given(redissonClient.getFairLock(any())).willReturn(mockLock);
    given(mockLock.tryLock(anyLong(), any(TimeUnit.class))).willReturn(true);
    given(appConfig.getDataGrid()).willReturn(mockDataGrid);

    // When
    exportScheduler.scheduleExport();

    // Then
    verify(notificationFileCreator)
        .publishNotificationFile(
            surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "first_file_1_1");
    verify(notificationFileCreator)
        .publishNotificationFile(
            surveyRefExerciseRef, templateMappingsWithActionType("BSNOT"), "first_file_1_1");
  }
}
