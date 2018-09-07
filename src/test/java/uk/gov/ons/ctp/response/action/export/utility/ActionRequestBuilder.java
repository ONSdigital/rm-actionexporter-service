package uk.gov.ons.ctp.response.action.export.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

public class ActionRequestBuilder {
  public static ActionRequest createSocialActionRequest(final String actionType) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setSampleUnitRef("sampleUR");
    actionAddress.setLine1("Prem1");
    actionAddress.setCountry("E");
    actionAddress.setOrganisationName("Castle of Frankenstein");
    actionAddress.setPostcode("postCode");
    actionAddress.setTownName("postTown");
    actionAddress.setLocality("locality");

    ActionRequest actionRequest = new ActionRequest();
    actionRequest.setActionId(UUID.randomUUID().toString());
    actionRequest.setActionPlan("actionPlan");
    actionRequest.setActionType(actionType);
    actionRequest.setAddress(actionAddress);
    actionRequest.setQuestionSet("questions");
    actionRequest.setLegalBasis("legalBasis");
    actionRequest.setRegion("region");
    actionRequest.setRespondentStatus("rStatus");
    actionRequest.setEnrolmentStatus("eStatus");
    actionRequest.setCaseGroupStatus("cgStatus");
    actionRequest.setCaseId(UUID.randomUUID().toString());
    actionRequest.setPriority(Priority.HIGHEST);
    actionRequest.setCaseRef("caseRef");
    actionRequest.setIac("test-iac");
    actionRequest.setExerciseRef("exRef");
    actionRequest.setContact(new ActionContact());
    actionRequest.setEvents(new ActionEvent(Collections.singletonList("event1")));
    actionRequest.setReturnByDate(DateTimeFormatter.ofPattern("dd/MM").format(LocalDate.now()));

    return actionRequest;
  }
}
