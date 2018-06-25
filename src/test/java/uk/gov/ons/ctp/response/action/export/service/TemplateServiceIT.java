package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import com.jcraft.jsch.ChannelSftp;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageBase;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageSender;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TemplateServiceIT {

  @Autowired private AppConfig appConfig;

  @Autowired private DefaultSftpSessionFactory defaultSftpSessionFactory;

  private SimpleMessageSender simpleMessageSender;
  private SimpleMessageListener simpleMessageListener;

  @Before
  public void setUp() {
    Rabbitmq rabbitConfig = this.appConfig.getRabbitmq();
    simpleMessageSender =
        new SimpleMessageSender(
            rabbitConfig.getHost(),
            rabbitConfig.getPort(),
            rabbitConfig.getUsername(),
            rabbitConfig.getPassword());

    simpleMessageListener =
        new SimpleMessageListener(
            rabbitConfig.getHost(),
            rabbitConfig.getPort(),
            rabbitConfig.getUsername(),
            rabbitConfig.getPassword());
  }

  @Test
  public void testTemplateGeneratesCorrectPrintFileForSocial() throws Exception {
    // Given
    ActionRequest actionRequest = createSocialActionRequest("SOCIALNOT");

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        actionInstructionToXmlString(actionInstruction));

    // When
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    queue.take();

    // Then
    String notificationFilePath = getLatestSftpFileName();
    InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);

    Iterator<String> templateRow = readFirstCsvRow(inputSteam).iterator();

    assertEquals(actionRequest.getAddress().getLine1(), templateRow.next());
    assertThat(templateRow.next(), isEmptyString()); // Address line 2 should be empty
    assertEquals(actionRequest.getAddress().getPostcode(), templateRow.next());
    assertEquals(actionRequest.getAddress().getTownName(), templateRow.next());
    assertEquals(actionRequest.getAddress().getLocality(), templateRow.next());
    assertEquals(actionRequest.getIac(), templateRow.next());
    assertEquals(actionRequest.getCaseRef(), templateRow.next());

    // Delete the file created in this test
    defaultSftpSessionFactory.getSession().remove(notificationFilePath);
  }

  @Test
  public void testTemplateGeneratesCorrectPrintFileForSocialPreNotification() throws Exception {
    // Given
    ActionRequest actionRequest = createSocialActionRequest("SOCIALPRENOT");

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        actionInstructionToXmlString(actionInstruction));

    // When
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    queue.take();

    // Then
    String notificationFilePath = getLatestSftpFileName();
    InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);

    Iterator<String> templateRow = readFirstCsvRow(inputSteam).iterator();

    String notificationFile = StringUtils.substringAfterLast(notificationFilePath, "/");
    assertEquals("SOCIALPRENOT", StringUtils.substringBefore(notificationFile, "_"));

    assertEquals(actionRequest.getAddress().getLine1(), templateRow.next());
    assertThat(templateRow.next(), isEmptyString()); // Address line 2 should be empty
    assertEquals(actionRequest.getAddress().getPostcode(), templateRow.next());
    assertEquals(actionRequest.getAddress().getTownName(), templateRow.next());
    assertEquals(actionRequest.getAddress().getLocality(), templateRow.next());
    assertEquals(actionRequest.getCaseRef(), templateRow.next());

    // Delete the file created in this test
    defaultSftpSessionFactory.getSession().remove(notificationFilePath);
  }

  private String actionInstructionToXmlString(ActionInstruction actionInstruction)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(actionInstruction, stringWriter);
    return stringWriter.toString();
  }

  private String getLatestSftpFileName() throws IOException {
    final String sftpPath = "Documents/sftp/";
    ChannelSftp.LsEntry[] sftpList = defaultSftpSessionFactory.getSession().list(sftpPath);
    Arrays.sort(sftpList, Comparator.comparingInt(o -> o.getAttrs().getMTime()));
    return sftpPath + sftpList[sftpList.length - 1].getFilename();
  }

  private CSVRecord readFirstCsvRow(InputStream inputStream) throws IOException {
    try (Reader reader = new InputStreamReader(inputStream);
        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(':'))) {
      return parser.iterator().next();
    }
  }

  private ActionRequest createSocialActionRequest(final String actionType) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setSampleUnitRef("sampleUR");
    actionAddress.setLine1("Prem1");
    actionAddress.setPostcode("postCode");
    actionAddress.setTownName("postTown");
    actionAddress.setLocality("locality");

    ActionRequest actionRequest = new ActionRequest();
    actionRequest.setActionId(UUID.randomUUID().toString());
    actionRequest.setActionPlan("actionPlan");
    actionRequest.setActionType(actionType);
    actionRequest.setAddress(actionAddress);
    actionRequest.setQuestionSet("questions");
    actionRequest.setLegalBasis("legalBasis");
    actionRequest.setRegion("region");
    actionRequest.setRespondentStatus("rStatus");
    actionRequest.setEnrolmentStatus("eStatus");
    actionRequest.setCaseGroupStatus("cgStatus");
    actionRequest.setCaseId(UUID.randomUUID().toString());
    actionRequest.setPriority(Priority.HIGHEST);
    actionRequest.setCaseRef("caseRef");
    actionRequest.setIac("test-iac");
    actionRequest.setExerciseRef("exRef");
    actionRequest.setContact(new ActionContact());
    actionRequest.setEvents(new ActionEvent(Collections.singletonList("event1")));

    return actionRequest;
  }
}
