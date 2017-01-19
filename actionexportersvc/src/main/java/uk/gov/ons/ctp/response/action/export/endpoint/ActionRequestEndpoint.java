package uk.gov.ons.ctp.response.action.export.endpoint;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.representation.ActionRequestInstructionDTO;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.TransformationService;

/**
 * The REST endpoint controller for ActionRequests.
 */
@Path("/actionrequests")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ActionRequestEndpoint {

  public static final String ACTION_REQUEST_NOT_FOUND = "ActionRequest not found for actionId";

  public static final String ACTION_REQUEST_TRANSFORM_ERROR = "Error transforming ActionRequest for actionId";

  @Inject
  private ActionRequestService actionRequestService;

  @Inject
  private TransformationService transformationService;

  @Inject
  private SftpServicePublisher sftpService;

  @Inject
  private MapperFacade mapperFacade;

  @Context
  private UriInfo uriInfo;

  /**
   * To retrieve all ActionRequests
   * 
   * @return a list of ActionRequests
   */
  @GET
  @Path("/")
  public List<ActionRequestInstructionDTO> findAllActionRequests() {
    log.debug("Entering findAllActionRequests ...");
    List<ActionRequestInstruction> actionRequests = actionRequestService.retrieveAllActionRequests();
    List<ActionRequestInstructionDTO> results = mapperFacade.mapAsList(actionRequests,
        ActionRequestInstructionDTO.class);
    return CollectionUtils.isEmpty(results) ? null : results;
  }

  /**
   * To retrieve a specific ActionRequest
   * 
   * @param actionId for the specific ActionRequest to retrieve
   * @return the specific ActionRequest
   * @throws CTPException if no ActionRequest found
   */
  @GET
  @Path("/{actionId}")
  public ActionRequestInstructionDTO findActionRequest(@PathParam("actionId") final BigInteger actionId)
      throws CTPException {
    log.debug("Entering findActionRequest with {}", actionId);
    ActionRequestInstruction result = actionRequestService.retrieveActionRequest(actionId);
    if (result == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %d", ACTION_REQUEST_NOT_FOUND, actionId));
    }
    return mapperFacade.map(result, ActionRequestInstructionDTO.class);
  }

  /**
   * To export a specific ActionRequest
   * 
   * @param actionId the actionId of the specific ActionRequest
   * @return 201 if successful
   * @throws CTPException if specific ActionRequest not found
   */
  @POST
  @Path("/{actionId}")
  public Response export(@PathParam("actionId") final BigInteger actionId) throws CTPException {
    log.debug("Entering export with actionId {}", actionId);
    ActionRequestInstruction actionRequest = actionRequestService.retrieveActionRequest(actionId);
    if (actionRequest == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %d", ACTION_REQUEST_NOT_FOUND, actionId));
    }
    ExportMessage message = new ExportMessage();
    transformationService.processActionRequest(message, actionRequest);
    if (message.isEmpty()) {
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR,
          String.format("%s %d", ACTION_REQUEST_TRANSFORM_ERROR, actionId));
    }
    message.getOutputStreams().forEach((fileName, stream) -> {
      sftpService.sendMessage(fileName, message.getActionRequestIds(fileName), stream);
    });

    UriBuilder ub = uriInfo.getAbsolutePathBuilder();
    URI actionRequestUri = ub.build();
    ActionRequestInstructionDTO actionRequestDTO = mapperFacade.map(actionRequest,
        ActionRequestInstructionDTO.class);
    return Response.created(actionRequestUri).entity(actionRequestDTO).build();
  }
}