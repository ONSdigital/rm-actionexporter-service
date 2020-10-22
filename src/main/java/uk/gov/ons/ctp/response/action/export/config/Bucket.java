package uk.gov.ons.ctp.response.action.export.config;

import lombok.Data;

@Data
public class Bucket {
  private String name;
  private String prefix;
}
