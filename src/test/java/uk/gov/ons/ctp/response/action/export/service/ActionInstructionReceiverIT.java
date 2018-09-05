package uk.gov.ons.ctp.response.action.export.service;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.response.action.export.utility.ActionRequestBuilder;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ActionInstructionReceiverIT {
  @LocalServerPort private int port;

  @Autowired private MessageChannel actionInstructionTransformed;

  @MockBean private ActionExportService actionExportService;

  @Rule
  public WireMockRule wireMockRule =
      new WireMockRule(options().extensions(new ResponseTemplateTransformer(false)).port(18002));

  @Test(expected = MessageHandlingException.class)
  public void ensureFiniteRetriesOnFailedActionInstruction() throws Exception {
    doThrow(RuntimeException.class).when(actionExportService).acceptInstruction(any());

    ActionRequest actionRequest = ActionRequestBuilder.createSocialActionRequest("SOCIALPRENOT");
    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    Message<ActionInstruction> caseMessage = new GenericMessage<>(actionInstruction);
    actionInstructionTransformed.send(caseMessage);

    verify(actionExportService, times(3)).acceptInstruction(any());
  }
}
