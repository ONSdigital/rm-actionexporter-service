package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.jcraft.jsch.ChannelSftp;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
  private static final Logger log = LoggerFactory.getLogger(TemplateServiceIT.class);

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

    try (Reader reader = new InputStreamReader(inputSteam);
        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(':'))) {
      Iterator<String> templateRow = parser.iterator().next().iterator();
      assertEquals("\n", parser.getFirstEndOfLine());
      assertEquals(actionRequest.getAddress().getLine1(), templateRow.next());
      assertThat(templateRow.next(), isEmptyString()); // Address line 2 should be empty
      assertEquals(actionRequest.getAddress().getPostcode(), templateRow.next());
      assertEquals(actionRequest.getAddress().getTownName(), templateRow.next());
      assertEquals(actionRequest.getAddress().getLocality(), templateRow.next());
      assertEquals(actionRequest.getAddress().getCountry(), templateRow.next());
      assertEquals(actionRequest.getIac(), templateRow.next());
      assertEquals(actionRequest.getAddress().getSampleUnitRef(), templateRow.next());
      assertEquals(actionRequest.getReturnByDate(), templateRow.next());
    } finally {
      // Delete the file created in this test
      assertTrue(defaultSftpSessionFactory.getSession().remove(notificationFilePath));
    }
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

    try (Reader reader = new InputStreamReader(inputSteam);
        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(':'))) {

      String notificationFile = StringUtils.substringAfterLast(notificationFilePath, "/");
      assertEquals("SOCIALPRENOT", StringUtils.substringBefore(notificationFile, "_"));

      Iterator<String> templateRow = parser.iterator().next().iterator();
      assertEquals("\n", parser.getFirstEndOfLine());
      assertEquals(actionRequest.getAddress().getLine1(), templateRow.next());
      assertThat(templateRow.next(), isEmptyString()); // Line 2 should be empty
      assertEquals(actionRequest.getAddress().getPostcode(), templateRow.next());
      assertEquals(actionRequest.getAddress().getTownName(), templateRow.next());
      assertEquals(actionRequest.getAddress().getLocality(), templateRow.next());
      assertEquals(actionRequest.getAddress().getCountry(), templateRow.next());
      assertEquals(actionRequest.getAddress().getSampleUnitRef(), templateRow.next());
    } finally {
      // Delete the file created in this test
      assertTrue(defaultSftpSessionFactory.getSession().remove(notificationFilePath));
    }
  }

  private String actionInstructionToXmlString(ActionInstruction actionInstruction)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(actionInstruction, stringWriter);
    return stringWriter.toString();
  }

  private String getLatestSftpFileName() throws IOException {
    Comparator<ChannelSftp.LsEntry> sortByModifiedTimeDescending =
        (f1, f2) -> Integer.compare(f2.getAttrs().getMTime(), f1.getAttrs().getMTime());

    String sftpPath = "Documents/sftp/";
    ChannelSftp.LsEntry[] sftpList = defaultSftpSessionFactory.getSession().list(sftpPath);
    ChannelSftp.LsEntry latestFile =
        Arrays.stream(sftpList)
            .filter(f -> f.getFilename().endsWith(".csv"))
            .min(sortByModifiedTimeDescending)
            .orElseThrow(() -> new RuntimeException("No file on SFTP"));
    log.info("Found latest file={}", latestFile.getFilename());
    return sftpPath + latestFile.getFilename();
  }

  private ActionRequest createSocialActionRequest(final String actionType) {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setSampleUnitRef("sampleUR");
    actionAddress.setLine1("Prem1");
    actionAddress.setCountry("E");

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
    actionRequest.setReturnByDate(DateTimeFormatter.ofPattern("dd/MM").format(LocalDate.now()));

    return actionRequest;
  }
}
