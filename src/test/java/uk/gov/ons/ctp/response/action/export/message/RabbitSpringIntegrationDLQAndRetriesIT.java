package uk.gov.ons.ctp.response.action.export.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.utility.ActionRequestBuilder;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.tools.rabbit.SimpleMessageBase.ExchangeType;
import uk.gov.ons.tools.rabbit.SimpleMessageListener;
import uk.gov.ons.tools.rabbit.SimpleMessageSender;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class RabbitSpringIntegrationDLQAndRetriesIT {

  @LocalServerPort private int port;

  @Autowired AppConfig appConfig;

  @Autowired private ResourceLoader resourceLoader;

  private SimpleMessageListener listener;
  private SimpleMessageSender sender;

  @Before
  public void setup() {
    sender =
        new SimpleMessageSender(
            appConfig.getRabbitmq().getHost(), appConfig.getRabbitmq().getPort(),
            appConfig.getRabbitmq().getUsername(), appConfig.getRabbitmq().getPassword());

    listener =
        new SimpleMessageListener(
            appConfig.getRabbitmq().getHost(), appConfig.getRabbitmq().getPort(),
            appConfig.getRabbitmq().getUsername(), appConfig.getRabbitmq().getPassword());
  }

  @Test
  public void ensureFailedActionInstructionIsRetried() throws Exception {
    ActionRequest actionreq = ActionRequestBuilder.createSocialActionRequest("MEOWMEOW");
    actionreq.setActionId("MEOWMEOW");
    ActionInstruction actioninstr = new ActionInstruction();
    actioninstr.setActionRequest(actionreq);
    JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);

    sender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actioninstr));

    String message =
        listener
            .listen(ExchangeType.Direct, "action-deadletter-exchange", "Action.Printer.binding")
            .poll(30, TimeUnit.SECONDS);

    assertThat(message).isNotNull();

    ActionInstruction received =
        (ActionInstruction)
            jaxbContext
                .createUnmarshaller()
                .unmarshal(new ByteArrayInputStream(message.getBytes()));

    assertEquals(received.getActionRequest().getCaseId(), actionreq.getCaseId());
  }
}
