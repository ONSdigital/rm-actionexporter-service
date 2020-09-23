package uk.gov.ons.ctp.response.action.export.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFile;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFileEntry;
import uk.gov.ons.ctp.response.action.export.utility.ObjectBuilder;

/** */
@RunWith(MockitoJUnitRunner.class)
public class PrintFileServiceTest {

  @InjectMocks private PrintFileService printFileService;

  @Test
  public void testConvertToPrintFile() {
    List<ActionRequestInstruction> actionRequestInstructions =
        ObjectBuilder.buildListOfActionRequests();

    PrintFile printFile = printFileService.convertToPrintFile(actionRequestInstructions);

    assertNotNull(printFile);

    for (PrintFileEntry entry : printFile.getPrintFileEntries()) {
      assertEquals("testIac", entry.getIac());
      assertEquals("testCaseGroupStatus", entry.getCaseGroupStatus());
      assertEquals("testEnrolmentStatus", entry.getEnrolmentStatus());
      assertEquals("testRegion", entry.getRegion());
      assertEquals("testRespondentStatus", entry.getRespondentStatus());
      assertEquals("testSampleUnitRef", entry.getSampleUnitRef());
      assertEquals("testEmailAddress", entry.getContact().getEmailAddress());
      assertEquals("testForename", entry.getContact().getForename());
      assertEquals("testSurname", entry.getContact().getSurname());
    }
  }
}
