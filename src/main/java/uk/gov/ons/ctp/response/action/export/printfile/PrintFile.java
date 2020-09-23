package uk.gov.ons.ctp.response.action.export.printfile;

import java.util.List;
import lombok.Data;

/**
 * Represents a print file request which is sent to the ras-rm-print-file service
 *
 * <p>[{ "sampleUnitRef":"10001", "iac":"ai9bt497r7bn", "caseGroupStatus":"NOTSTARTED",
 * "enrolmentStatus":"", "respondentStatus":"", "contact":{ "forename":"Jon", "surname":"Snow",
 * "emailAddress":"jon.snow@example.com" }, "region":"HH" }]
 */
@Data
public class PrintFile {
  private List<PrintFileEntry> printFileEntries;
}
