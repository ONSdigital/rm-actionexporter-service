package uk.gov.ons.ctp.response.action.export.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;

/**
 * Service implementation responsible for publishing an action feedback message to the action
 * service.
 */
@MessageEndpoint
public class ActionFeedbackPublisher {
  private static final Logger log = LoggerFactory.getLogger(ActionFeedbackPublisher.class);

  @Qualifier("actionFeedbackRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * To publish an ActionFeedback message
   *
   * @param actionFeedback the ActionFeedback to publish.
   */
  public void sendActionFeedback(ActionFeedback actionFeedback) {
    log.debug(
        "action_id: "
            + actionFeedback.getActionId()
            + ", Entering sendActionFeedback for actionId ");
    rabbitTemplate.convertAndSend(actionFeedback);
  }
}
