package uk.gov.ons.ctp.response.action.export.domain;

public class SurveyRefExerciseRef {

        private String surveyRef;
        private String exerciseRef;

    public SurveyRefExerciseRef(String surveyRef, String exerciseRef) {
        this.surveyRef = surveyRef;
        this.exerciseRef = exerciseRef;
    }

    public void setSurveyRef(String surveyRef) {
        this.surveyRef = surveyRef;
    }

    public void setExerciseRef(String exerciseRef) {
        this.exerciseRef = exerciseRef;
    }

    public String getSurveyRef() {

        return surveyRef;
    }

    public String getExerciseRef() {
        return exerciseRef;
    }
}

