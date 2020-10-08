package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;

/** The apps main holder for centralized config read from application.yml or env vars */
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {
  private Rabbitmq rabbitmq;
  private Logging logging;
  private GCS gcs;
}
