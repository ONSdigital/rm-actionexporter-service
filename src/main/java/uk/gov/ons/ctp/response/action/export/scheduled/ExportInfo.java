package uk.gov.ons.ctp.response.action.export.scheduled;

import java.util.LinkedList;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Info returned to Spring boot actuator available at health endpoint as configured in application
 * under management e.g. /health
 */
@Component
@Data
public class ExportInfo {

  private static final int OUTCOME_SIZE = 30;
  private LinkedList<String> outcomes = new LinkedList<String>();

  /**
   * Add last export execution outcome
   *
   * @param outcome Details of last scheduled export action
   */
  public void addOutcome(String outcome) {
    if (outcomes.size() >= OUTCOME_SIZE) {
      outcomes.removeLast();
    }
    outcomes.addFirst(outcome);
  }
}
