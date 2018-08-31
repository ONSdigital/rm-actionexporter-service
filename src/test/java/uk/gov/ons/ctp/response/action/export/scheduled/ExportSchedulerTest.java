package uk.gov.ons.ctp.response.action.export.scheduled;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.action.export.TemplateMappings.templateMappingsWithActionType;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.distributed.DistributedLockManager;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.NotificationFileCreator;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

@RunWith(MockitoJUnitRunner.class)
public class ExportSchedulerTest {

  @Mock private DistributedLockManager actionExportLockManager;
  @Mock private TemplateMappingService templateMappingService;
  @Mock private ActionRequestService actionRequestService;
  @Mock private NotificationFileCreator notificationFileCreator;
  @InjectMocks private ExportScheduler exportScheduler;

  @Test
  public void shouldLockOnFilenameAndCollectionExercise() {
    // Given
    given(actionRequestService.retrieveDistinctExerciseRefsWithSurveyRef())
        .willReturn(Collections.singletonList(new SurveyRefExerciseRef("1", "1")));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(Collections.singletonMap("filename", templateMappingsWithActionType("BSNOT")));
    given(actionExportLockManager.lock(any())).willReturn(true);

    // When
    exportScheduler.scheduleExport();

    // Then
    verify(actionExportLockManager).lock("filename_1_1");
    verify(actionExportLockManager).unlock("filename_1_1");
  }

  @Test(expected = Exception.class)
  public void shouldUnlockWhenExceptionThrown() {
    // Given
    given(actionRequestService.retrieveDistinctExerciseRefsWithSurveyRef())
        .willReturn(Collections.singletonList(new SurveyRefExerciseRef("1", "1")));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(Collections.singletonMap("filename", templateMappingsWithActionType("BSNOT")));
    given(actionExportLockManager.lock(any())).willReturn(true);
    doThrow(Exception.class)
        .when(notificationFileCreator)
        .publishNotificationFile(any(), anyListOf(TemplateMapping.class), any());

    // When
    exportScheduler.scheduleExport();

    // Then
    verify(actionExportLockManager).lock("filename_1_1");
    verify(actionExportLockManager).unlock("filename_1_1");
  }

  @Test
  public void shouldFileForAllTemplateMappings() {
    // Given
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("1", "1");
    given(actionRequestService.retrieveDistinctExerciseRefsWithSurveyRef())
        .willReturn(Collections.singletonList(surveyRefExerciseRef));
    given(templateMappingService.retrieveAllTemplateMappingsByFilename())
        .willReturn(
            ImmutableMap.of(
                "first_file",
                templateMappingsWithActionType("BSNOT"),
                "second_file",
                templateMappingsWithActionType("BSPRENOT")));
    given(actionExportLockManager.lock(any())).willReturn(true);

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
