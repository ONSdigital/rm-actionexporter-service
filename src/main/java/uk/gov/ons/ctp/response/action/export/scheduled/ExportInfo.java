package uk.gov.ons.ctp.response.action.export.scheduled;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * Info returned to Spring boot actuator available at health endpoint as
 * configured in application under management e.g. /health
 *
 */
@CoverageIgnore
@Component
@Data
public class ExportInfo {

  private static final int OUTCOME_SIZE = 30;
  private LinkedList<String> outcomes = new LinkedList<String>();

  /**
   * Add last export execution outcome
   *
   * @param outcome Details of last scheduled export action
   *
   */
  public void addOutcome(String outcome) {
    if (outcomes.size() >= OUTCOME_SIZE) {
      outcomes.removeLast();
    }
    outcomes.addFirst(outcome);
  }
}
