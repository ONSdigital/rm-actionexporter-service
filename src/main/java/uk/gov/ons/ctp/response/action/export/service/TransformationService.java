package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.ArrayList;
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

/** The implementation of TransformationService */
@Service
public class TransformationService {
  private static final Logger log = LoggerFactory.getLogger(TransformationService.class);

  @Autowired private TemplateService templateService;

  @Autowired private TemplateMappingService templateMappingService;

  public ExportMessage processActionRequests(
      ExportMessage message, List<ActionRequestInstruction> requests) throws CTPException {
    return buildExportMessage(message, requests);
  }

  public ExportMessage processActionRequest(
      ExportMessage message, ActionRequestInstruction actionRequest) throws CTPException {
    List<ActionRequestInstruction> requests = new ArrayList<>();
    requests.add(actionRequest);
    return buildExportMessage(message, requests);
  }

  /**
   * Produces ExportMessage with stream objects and list of ActionRequest Ids. Assumes actionTypes
   * being processed are unique and not already in ExportMessage passed in to be built, if are
   * already present will be replaced.
   *
   * @param message to build
   * @param actionRequestList the list to be processed
   * @return ExportMessage with stream objects and list of ActionRequest Ids.
   * @throws CTPException if cannot retrieve TemplateMapping.
   */
  private ExportMessage buildExportMessage(
      ExportMessage message, List<ActionRequestInstruction> actionRequestList) throws CTPException {

    // if nothing to process return ExportMessage
    if (actionRequestList.isEmpty()) {
      return message;
    }

    Map<String, TemplateMapping> mapping =
        templateMappingService.retrieveAllTemplateMappingsByActionType();
    Map<String, List<ActionRequestInstruction>> templateRequests =
        actionRequestList
            .stream()
            .collect(Collectors.groupingBy(ActionRequestInstruction::getActionType));
    templateRequests.forEach(
        (actionType, actionRequests) -> {
          if (mapping.containsKey(actionType)) {
            try {
              message
                  .getOutputStreams()
                  .put(
                      actionType,
                      templateService.stream(
                          actionRequests, mapping.get(actionType).getTemplate()));
              List<UUID> addActionIds = new ArrayList<UUID>();
              message.getActionRequestIds().put(actionType, addActionIds);
              actionRequests.forEach(
                  (actionRequest) -> {
                    addActionIds.add(actionRequest.getActionId());
                  });
            } catch (CTPException e) {
              // catch failure for templateService stream operation for that actionType but try
              // others, if any.
              log.with("action_type", actionType).error("Error generating actionType", e);
            }
          } else {
            log.with("action_type", actionType).warn("No mapping for actionType");
          }
        });
    return message;
  }
}
