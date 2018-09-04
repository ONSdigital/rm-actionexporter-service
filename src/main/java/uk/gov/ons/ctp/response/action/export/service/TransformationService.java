package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;

@Service
public class TransformationService {
  private static final Logger log = LoggerFactory.getLogger(TransformationService.class);

  @Autowired private TemplateService templateService;

  @Autowired private TemplateMappingService templateMappingService;

  /**
   * Produces ExportMessage with stream objects and list of ActionRequest Ids. Assumes actionTypes
   * being processed are unique and not already in ExportMessage passed in to be built, if are
   * already present will be replaced.
   *
   * @param allActionRequest the list to be processed
   * @return ExportMessage with stream objects and list of ActionRequest Ids.
   */
  public ExportMessage processActionRequests(List<ActionRequestInstruction> allActionRequest) {
    ExportMessage message = new ExportMessage();
    if (allActionRequest.isEmpty()) {
      return message;
    }
    Map<String, TemplateMapping> mapping =
        templateMappingService.retrieveAllTemplateMappingsByActionType();

    Map<String, List<ActionRequestInstruction>> templateRequestsForActionType =
        allActionRequest
            .stream()
            .collect(Collectors.groupingBy(ActionRequestInstruction::getActionType));

    templateRequestsForActionType.forEach(
        (actionType, actionRequests) ->
            processRequestsForActionType(actionRequests, mapping, actionType, message));
    return message;
  }

  private void processRequestsForActionType(
      List<ActionRequestInstruction> actionRequests,
      Map<String, TemplateMapping> mapping,
      String actionType,
      ExportMessage message) {
    if (!mapping.containsKey(actionType)) {
      String actionPlanIds =
          actionRequests
              .stream()
              .map(ar -> ar.getActionrequestPK().toString())
              .collect(Collectors.joining(","));
      log.with("action_type", actionType)
          .with("action_plan_ids", actionPlanIds)
          .warn("No mapping for actionType. Cannot process action requests");
      return;
    }

    try {
      ByteArrayOutputStream renderedTemplateStream =
          templateService.stream(actionRequests, mapping.get(actionType).getTemplate());
      message.getOutputStreams().put(actionType, renderedTemplateStream);
      List<UUID> actionIds =
          actionRequests
              .stream()
              .map(ActionRequestInstruction::getActionId)
              .collect(Collectors.toList());
      message.getActionRequestIds().put(actionType, actionIds);
    } catch (CTPException e) {
      log.with("action_type", actionType).error("Error generating actionType", e);
    }
  }
}
