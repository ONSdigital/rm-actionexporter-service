package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.jcraft.jsch.ChannelSftp;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
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
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.utility.ActionRequestBuilder;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.tools.rabbit.SimpleMessageBase;
import uk.gov.ons.tools.rabbit.SimpleMessageListener;
import uk.gov.ons.tools.rabbit.SimpleMessageSender;

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
  public void testTemplateGeneratesCorrectReminderFileForSocial() throws Exception {
    // Given
    ActionRequest actionRequest = ActionRequestBuilder.createSocialActionRequest("SOCIALREM");

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actionInstruction));

    // When
    String message = queue.take();

    // Then
    assertThat(message, containsString("SOCIALREM"));
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
      assertEquals(actionRequest.getAddress().getOrganisationName(), templateRow.next());
      assertEquals(actionRequest.getAddress().getSampleUnitRef(), templateRow.next());
      assertEquals(actionRequest.getReturnByDate(), templateRow.next());
    } finally {
      // Delete the file created in this test
      assertTrue(defaultSftpSessionFactory.getSession().remove(notificationFilePath));
    }
  }

  @Test
  public void testTemplateGeneratesCorrectPrintFileForSocial() throws Exception {
    // Given
    ActionRequest actionRequest = ActionRequestBuilder.createSocialActionRequest("SOCIALNOT");

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actionInstruction));

    // When
    String message = queue.take();

    // Then
    assertThat(message, containsString("SOCIALNOT"));
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
    ActionRequest actionRequest = ActionRequestBuilder.createSocialActionRequest("SOCIALPRENOT");

    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);
    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(actionInstruction));

    // When
    String message = queue.take();

    // Then
    assertThat(message, containsString("SOCIALPRENOT"));
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

  @Test
  public void testMostRecentAddressUsedWhenDuplicateSampleUnitRefs() throws Exception {
    // Given
    ActionInstruction firstActionInstruction =
        createActionInstruction("SOCIALREM", "Old Address", "exercise_1");
    ActionInstruction secondActionInstruction =
        createActionInstruction("SOCIALREM", "New Address", "exercise_2");

    BlockingQueue<String> queue =
        simpleMessageListener.listen(
            SimpleMessageBase.ExchangeType.Fanout, "event-message-outbound-exchange");

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(firstActionInstruction));

    // When
    String firstActionExportConfirmation = queue.take();

    assertThat(firstActionExportConfirmation, containsString("SOCIALREM"));
    String firstNotificationFilePath = getLatestSftpFileName();
    assertTrue(defaultSftpSessionFactory.getSession().remove(firstNotificationFilePath));
    defaultSftpSessionFactory.getSession().close();

    simpleMessageSender.sendMessage(
        "action-outbound-exchange",
        "Action.Printer.binding",
        ActionRequestBuilder.actionInstructionToXmlString(secondActionInstruction));

    String secondActionExportConfirmation = queue.take();

    // Then
    assertThat(secondActionExportConfirmation, containsString("SOCIALREM"));
    String secondNotificationFilePath = getLatestSftpFileName();
    InputStream inputSteam =
        defaultSftpSessionFactory.getSession().readRaw(secondNotificationFilePath);

    try (Reader reader = new InputStreamReader(inputSteam);
        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(':'))) {
      Iterator<String> firstRowColumns = parser.iterator().next().iterator();
      assertEquals("New Address", firstRowColumns.next());
    } finally {
      // Delete the file created in this test
      assertTrue(defaultSftpSessionFactory.getSession().remove(secondNotificationFilePath));
    }
  }

  private ActionInstruction createActionInstruction(
      String actionType, String addressLine1, String exerciseRef) {
    ActionRequest actionRequest =
        ActionRequestBuilder.createSocialActionRequest(actionType, addressLine1, exerciseRef);
    ActionInstruction actionInstruction = new ActionInstruction();
    actionInstruction.setActionRequest(actionRequest);

    return actionInstruction;
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
    log.with("latest_file", latestFile.getFilename()).info("Found latest file");
    return sftpPath + latestFile.getFilename();
  }
}
