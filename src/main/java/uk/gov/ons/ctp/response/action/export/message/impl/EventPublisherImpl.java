package uk.gov.ons.ctp.response.action.export.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;

@MessageEndpoint
@Slf4j
public class EventPublisherImpl implements EventPublisher {

  @Qualifier("amqpTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void publishEvent(String event) {
    log.info("Publish Event action exporter {}", event);
    rabbitTemplate.convertAndSend(event);
  }
}
