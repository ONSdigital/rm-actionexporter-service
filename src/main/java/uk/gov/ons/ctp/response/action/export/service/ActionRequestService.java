package uk.gov.ons.ctp.response.action.export.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;

/** The implementation of ActionRequestService */
@Service
@Slf4j
public class ActionRequestService {

  @Autowired private ActionRequestRepository repository;

  public List<ActionRequestInstruction> retrieveAllActionRequests() {
    return repository.findAll();
  }

  public ActionRequestInstruction retrieveActionRequest(UUID actionId) {
    return repository.findByActionId(actionId);
  }

  public ActionRequestInstruction save(final ActionRequestInstruction actionRequest) {
    log.debug("Saving ActionRequest {}", actionRequest.getActionId());
    return repository.save(actionRequest);
  }

  public List<ActionRequestInstruction> findByDateSentIsNullAndActionTypeAndExerciseRef(
      String actionType, String exerciseRef, String surveyRef) {
    return repository.findByDateSentIsNullAndActionTypeAndExerciseRefAndSurveyRef(
        actionType, exerciseRef, surveyRef);
  }

  public List<String> retrieveExerciseRefs() {
    return repository.findAllExerciseRefs();
  }

  public List<SurveyRefExerciseRef> retrieveDistinctExerciseRefsWithSurveyRef() {
    return repository.findDistinctSurveyAndExerciseRefs();
  }

  public List<String> retrieveActionTypes() {
    return repository.findAllActionType();
  }

  public int updateDateSentByActionId(Set<UUID> actionIds, Timestamp dateSent) {
    return repository.updateDateSentByActionId(actionIds, dateSent);
  }

  public List<UUID> retrieveResponseRequiredByActionId(Set<UUID> actionIds) {
    return repository.retrieveResponseRequiredByActionId(actionIds);
  }
}
