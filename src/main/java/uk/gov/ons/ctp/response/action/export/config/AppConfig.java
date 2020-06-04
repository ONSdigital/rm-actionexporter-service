package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;

/** The apps main holder for centralized config read from application.yml or env vars */
@CoverageIgnore
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {
  private Rabbitmq rabbitmq;
  private ExportSchedule exportSchedule;
  private DataGrid dataGrid;
  private SwaggerSettings swaggerSettings;
  private Logging logging;
  private GCS gcs;
}
