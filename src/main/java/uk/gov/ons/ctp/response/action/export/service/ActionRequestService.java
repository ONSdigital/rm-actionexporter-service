package uk.gov.ons.ctp.response.action.export.service;

import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.SurveyRefExerciseRef;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service responsible for dealing with ActionRequests
 */
public interface ActionRequestService {
  /**
   * To retrieve all ActionRequests
   *
   * @return a list of ActionRequests
   */
  List<ActionRequestInstruction> retrieveAllActionRequests();

  /**
   * To retrieve a given ActionRequest
   *
   * @param actionId the ActionRequest actionId to be retrieved
   * @return the given ActionRequest
   */
  ActionRequestInstruction retrieveActionRequest(UUID actionId);

  /**
   * Save an ActionRequest
   *
   * @param actionRequest the ActionRequest to save.
   * @return the ActionRequest saved.
   */
  ActionRequestInstruction save(ActionRequestInstruction actionRequest);

  /**
   * Retrieve all ActionRequests not sent for an actionType and exerciseRef.
   *
   * @param actionType actionType for which to retrieve ActionRequests.
   * @param exerciseRef for which to retrieve ActionRequests.
   * @return List of ActionRequests not sent to external services previously for
   *         actionType, exerciseRef.
   */
  List<ActionRequestInstruction> findByDateSentIsNullAndActionTypeAndExerciseRef(String actionType, String exerciseRef, String surveyRef);

  /**
   * Return a list of distinct exerciseRefs
   *
   * @return a list of exerciseRefs.
   */
  List<String> retrieveExerciseRefs();

  /**
   * Return a list of distinct exerciseRefs with associated surveyRef
   *
   * @return a list of exerciseRefs with SurveyRef.
   */
  List<SurveyRefExerciseRef> retrieveDistinctExerciseRefsWithSurveyRef();

  /**
   * Return a list of distinct actionTypes
   *
   * @return a list of actionTypes.
   */
  List<String> retrieveActionTypes();

  /**
   * Update ActionRequest date sent for batch of actionIds.
   *
   * @param actionIds List of ActionRequest actionIds to update.
   * @param dateSent to set on each ActionRequest.
   * @return int of affected rows
   */
  int updateDateSentByActionId(Set<UUID> actionIds, Timestamp dateSent);

  /**
   * Retrieve actionIds where response required is true for batch of actionIds.
   *
   * @param actionIds ActionRequest actionIds to check for response required.
   * @return actionIds of those where response required.
   */
  List<UUID> retrieveResponseRequiredByActionId(Set<UUID> actionIds);
}
