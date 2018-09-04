package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyRefExerciseRef {

  private String surveyRef;
  private String exerciseRef;

  /**
   * This checks the format of exerciseRef if it is survey_ref + exercise_ref e.g 221_201712 it
   * strips the survey_ref off. This is because the exercise_ref is set incorrectly in Collection
   * Exercise service.
   *
   * @return exerciseRef with surveyRef prefix stripped.
   */
  public String getExerciseRefWithoutSurveyRef() {
    String exerciseRefWithoutSurveyRef = StringUtils.substringAfter(exerciseRef, "_");
    return StringUtils.defaultIfEmpty(exerciseRefWithoutSurveyRef, exerciseRef);
  }
}
