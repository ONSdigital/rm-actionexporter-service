package uk.gov.ons.ctp.response.action.export.printfile;

import lombok.Data;

@Data
public class PrintFileEntry {

  private String sampleUnitRef;
  private String iac;
  private String caseGroupStatus;
  private String enrolmentStatus;
  private String respondentStatus;
  private Contact contact;
  private String region;
}
