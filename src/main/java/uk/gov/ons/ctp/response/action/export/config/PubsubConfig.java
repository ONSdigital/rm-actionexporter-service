package uk.gov.ons.ctp.response.action.export.config;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j
@Configuration
public class PubsubConfig {

  @Bean
  public Publisher publisherSupplier(
      @Value("${gcp.project}") String project, @Value("${gcp.topic}") String topic)
      throws IOException {
    log.info("creating pubsub publish for topic " + topic);
    TopicName topicName = TopicName.of(project, topic);
    return Publisher.newBuilder(topicName).build();
  }
}
