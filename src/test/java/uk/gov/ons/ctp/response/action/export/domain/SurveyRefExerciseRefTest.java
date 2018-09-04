package uk.gov.ons.ctp.response.action.export.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class SurveyRefExerciseRefTest {

  @Test
  public void shouldStripSurveyRef() {
    // Given
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("221", "221_201808");

    // When
    String exerciseRefWithoutSurveyRef = surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();

    // Then
    assertEquals("201808", exerciseRefWithoutSurveyRef);
  }

  @Test
  public void shouldReturnExerciseRef() {
    // Given
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("221", "201808");

    // When
    String exerciseRefWithoutSurveyRef = surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();

    // Then
    assertEquals("201808", exerciseRefWithoutSurveyRef);
  }

  @Test
  public void shouldKeepUnderscoreInCollectionExerciseRef() {
    // Given
    SurveyRefExerciseRef surveyRefExerciseRef = new SurveyRefExerciseRef("221", "221_2018_08");

    // When
    String exerciseRefWithoutSurveyRef = surveyRefExerciseRef.getExerciseRefWithoutSurveyRef();

    // Then
    assertEquals("2018_08", exerciseRefWithoutSurveyRef);
  }
}
