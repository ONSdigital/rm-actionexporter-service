package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;

/** Config POJO for GCS params */
@Data
public class GCP {
  private Bucket bucket;
}
