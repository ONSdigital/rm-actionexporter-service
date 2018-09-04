package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;

/** The implementation of ActionRequestService */
@Service
public class ActionRequestService {
  private static final Logger log = LoggerFactory.getLogger(ActionRequestService.class);

  @Autowired private ActionRequestRepository repository;

  public List<ActionRequestInstruction> retrieveAllActionRequests() {
    return repository.findAll();
  }

  public ActionRequestInstruction retrieveActionRequest(UUID actionId) {
    return repository.findByActionId(actionId);
  }

  public ActionRequestInstruction save(final ActionRequestInstruction actionRequest) {
    log.with("action_request", actionRequest.getActionId()).debug("Saving ActionRequest");
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

  public int updateDateSentByActionId(Set<UUID> actionIds, Timestamp dateSent) {
    return repository.updateDateSentByActionId(actionIds, dateSent);
  }

  public List<UUID> retrieveResponseRequiredByActionId(Set<UUID> actionIds) {
    return repository.retrieveResponseRequiredByActionId(actionIds);
  }
}
