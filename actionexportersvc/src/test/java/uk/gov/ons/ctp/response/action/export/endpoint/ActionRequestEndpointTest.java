package uk.gov.ons.ctp.response.action.export.endpoint;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.action.export.endpoint.ActionRequestEndpoint.ACTION_REQUEST_NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.action.ActionExporterBeanMapper;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportMessage;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.TransformationService;

/**
 * ActionRequestEndpoint unit tests
 */
public class ActionRequestEndpointTest {

  private final static String NON_EXISTING_ACTION_ID = "7bc5d41b-0549-40b3-ba76-42f6d4cf3fd1";
  
  private final static String EXISTING_ACTION_ID_1 = "7bc5d41b-0549-40b3-ba76-42f6d4cf3fd1";
  private final static String EXISTING_ACTION_ID_2 = "7bc5d41b-0549-40b3-ba76-42f6d4cf3fd2";
  private final static String EXISTING_ACTION_ID_3 = "7bc5d41b-0549-40b3-ba76-42f6d4cf3fd3";

  @InjectMocks
  private ActionRequestEndpoint actionRequestEndpoint;

  @Mock
  private ActionRequestService actionRequestService;

  @Mock
  private TransformationService transformationService;

  @Mock
  private SftpServicePublisher sftpService;

  @Spy
  private MapperFacade mapperFacade = new ActionExporterBeanMapper();

  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
            .standaloneSetup(actionRequestEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();
  }

  @Test
  public void findAllActionRequests() throws Exception {
    List<ActionRequestInstruction> result = new ArrayList<>();
 
    result.add(buildActionRequest(UUID.fromString(EXISTING_ACTION_ID_1)));
    result.add(buildActionRequest(UUID.fromString(EXISTING_ACTION_ID_2)));
    result.add(buildActionRequest(UUID.fromString(EXISTING_ACTION_ID_3)));
    
    when(actionRequestService.retrieveAllActionRequests()).thenReturn(result);

    ResultActions actions = mockMvc.perform(getJson("/actionrequests/"));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionRequestEndpoint.class))
            .andExpect(handler().methodName("findAllActionRequests"))
            .andExpect(jsonPath("$", Matchers.hasSize(3))).andExpect(jsonPath("$[*].actionId", containsInAnyOrder(new String(EXISTING_ACTION_ID_1), new String(EXISTING_ACTION_ID_2), new String(EXISTING_ACTION_ID_3))));
  }

  @Test
  public void findNonExistingActionRequest() throws Exception {
    ResultActions actions = mockMvc.perform(getJson(String.format("/actionrequests/%s", UUID.fromString(NON_EXISTING_ACTION_ID))));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionRequestEndpoint.class))
            .andExpect(handler().methodName("findActionRequest"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format("%s %s", ACTION_REQUEST_NOT_FOUND, NON_EXISTING_ACTION_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  @Test
  public void findExistingActionRequest() throws Exception {
    when(actionRequestService.retrieveActionRequest(UUID.fromString(EXISTING_ACTION_ID_1))).thenReturn(buildActionRequest(UUID.fromString(EXISTING_ACTION_ID_1)));

    ResultActions actions = mockMvc.perform(getJson(String.format("/actionrequests/%s", EXISTING_ACTION_ID_1)));

    actions.andExpect(status().isOk())
            .andExpect(handler().handlerType(ActionRequestEndpoint.class))
            .andExpect(handler().methodName("findActionRequest"))
            .andExpect(jsonPath("$.actionId", is(EXISTING_ACTION_ID_1)));
  }

  @Test
  public void exportNonExistingActionRequest() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/actionrequests/%s", NON_EXISTING_ACTION_ID), ""));

    actions.andExpect(status().isNotFound())
            .andExpect(handler().handlerType(ActionRequestEndpoint.class))
            .andExpect(handler().methodName("export"))
            .andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())))
            .andExpect(jsonPath("$.error.message", is(String.format("%s %s", ACTION_REQUEST_NOT_FOUND, NON_EXISTING_ACTION_ID))))
            .andExpect(jsonPath("$.error.timestamp", isA(String.class)));
  }

  @Test
  public void exportExistingActionRequest() throws Exception {
    when(actionRequestService.retrieveActionRequest(UUID.fromString(EXISTING_ACTION_ID_1))).thenReturn(buildActionRequest(UUID.fromString(EXISTING_ACTION_ID_1)));
    when(transformationService.processActionRequest(any(ExportMessage.class), any(ActionRequestInstruction.class))).thenAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      return buildSftpMessage((ExportMessage) args[0]);
    });
    when(sftpService.sendMessage(any(String.class), any(), any())).thenAnswer(invocation -> "Any string".getBytes());

    ResultActions actions = mockMvc.perform(postJson(String.format("/actionrequests/%s", EXISTING_ACTION_ID_1), ""));

    actions.andExpect(status().isCreated())
            .andExpect(handler().handlerType(ActionRequestEndpoint.class))
            .andExpect(handler().methodName("export"))
            .andExpect(jsonPath("$.actionId", is(EXISTING_ACTION_ID_1)));
  }

  private static ActionRequestInstruction buildActionRequest(UUID actionId) {
    ActionRequestInstruction actionRequest = new ActionRequestInstruction();
    actionRequest.setActionId(actionId);
    return actionRequest;
  }

  private ExportMessage buildSftpMessage(ExportMessage message) {
    message.getActionRequestIds().put("dummy", Collections.singletonList(UUID.fromString("7bc5d41b-0549-40b3-ba76-42f6d4cf3fd1")));
    message.getOutputStreams().put("dummy", new ByteArrayOutputStream());
    return message;
  }
}
