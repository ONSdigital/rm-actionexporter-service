package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyRefExerciseRef {

        private String surveyRef;
        private String exerciseRef;
}

