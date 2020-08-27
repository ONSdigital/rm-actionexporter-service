package uk.gov.ons.ctp.response.action.export.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
public class EventPublisher {
  private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

  @Qualifier("amqpTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  public void publishEvent(String event) {
    log.info("event: " + event + "Publish Event action exporter");
    rabbitTemplate.convertAndSend(event);
  }
}
