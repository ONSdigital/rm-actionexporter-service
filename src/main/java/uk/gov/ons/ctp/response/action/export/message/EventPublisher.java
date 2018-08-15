package uk.gov.ons.ctp.response.action.export.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
@Slf4j
public class EventPublisher {

  @Qualifier("amqpTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  public void publishEvent(String event) {
    log.info("Publish Event action exporter {}", event);
    rabbitTemplate.convertAndSend(event);
  }
}
