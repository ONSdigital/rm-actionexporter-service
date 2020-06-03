package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Config POJO for GCS params */
@CoverageIgnore
@Data
public class GCS {
  private String bucket;
  private boolean enabled;
}
