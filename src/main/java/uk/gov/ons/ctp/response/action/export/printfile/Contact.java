package uk.gov.ons.ctp.response.action.export.printfile;

import lombok.Data;

@Data
public class Contact {
  private String forename;
  private String surname;
  private String emailAddress;
}
