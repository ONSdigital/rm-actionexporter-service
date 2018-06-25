package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Config for ExportScheduler */
@CoverageIgnore
@Data
public class ExportSchedule {
  private String cronExpression;
}
