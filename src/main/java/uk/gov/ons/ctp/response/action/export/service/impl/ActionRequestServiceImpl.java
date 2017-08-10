package uk.gov.ons.ctp.response.action.export.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The implementation of ActionRequestService
 */
@Service
@Slf4j
public class ActionRequestServiceImpl implements ActionRequestService {

  @Autowired
  private ActionRequestRepository repository;

  @Override
  public List<ActionRequestInstruction> retrieveAllActionRequests() {
    return repository.findAll();
  }

  @Override
  public ActionRequestInstruction retrieveActionRequest(UUID actionId) {
    return repository.findByActionId(actionId);
  }

  @Override
  public ActionRequestInstruction save(final ActionRequestInstruction actionRequest) {
    log.debug("Saving ActionRequest {}", actionRequest.getActionId());
    return repository.save(actionRequest);
  }

  @Override
  public List<ActionRequestInstruction> findByDateSentIsNullAndActionTypeAndExerciseRef(String actionType,
      String exerciseRef) {
    return repository.findByDateSentIsNullAndActionTypeAndExerciseRef(actionType, exerciseRef);
  }

  @Override
  public List<String> retrieveExerciseRefs() {
    return repository.findAllExerciseRefs();
  }

  @Override
  public List<String> retrieveActionTypes() {
    return repository.findAllActionType();
  }

  @Override
  public int updateDateSentByActionId(Set<UUID> actionIds, Timestamp dateSent) {
    return repository.updateDateSentByActionId(actionIds, dateSent);
  }

  @Override
  public List<UUID> retrieveResponseRequiredByActionId(Set<UUID> actionIds) {
    return repository.retrieveResponseRequiredByActionId(actionIds);
  }
}
