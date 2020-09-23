package uk.gov.ons.ctp.response.action.export.service;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFileEntry;
import uk.gov.ons.ctp.response.action.export.utility.ObjectBuilder;

/** */
@RunWith(MockitoJUnitRunner.class)
public class PrintFileServiceTest {

  @Mock private Publisher publisher;
  @Mock ApiFuture<String> apiFuture;

  @InjectMocks private PrintFileService printFileService;

  @Test
  public void testConvertToPrintFile() {
    List<ActionRequestInstruction> actionRequestInstructions =
        ObjectBuilder.buildListOfActionRequests();

    List<PrintFileEntry> printFileEntries =
        printFileService.convertToPrintFile(actionRequestInstructions);

    assertFalse(printFileEntries.isEmpty());

    for (PrintFileEntry entry : printFileEntries) {
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

  @Test
  public void testSend() throws Exception {

    given(publisher.publish(any())).willReturn(apiFuture);
    given(apiFuture.get()).willReturn("test");
    List<ActionRequestInstruction> actionRequestInstructions =
        ObjectBuilder.buildListOfActionRequests();

    try {
      printFileService.send("test.csv", actionRequestInstructions);
    } catch (RuntimeException e) {
      fail(e.getMessage());
    }

    verify(publisher).publish(any());
    verify(apiFuture).get();
  }
}
