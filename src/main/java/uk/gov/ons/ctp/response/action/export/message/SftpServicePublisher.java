package uk.gov.ons.ctp.response.action.export.message;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Publisher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.GenericMessage;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportInfo;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;

/**
 * Service implementation responsible for publishing transformed ActionRequests via sftp. See Spring
 * Integration flow for details of sftp outbound channel.
 */
@MessageEndpoint
public class SftpServicePublisher {
  private static final Logger log = LoggerFactory.getLogger(SftpServicePublisher.class);

  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
  private static final String RESPONSES_REQUIRED = "response_required_id_list";
  private static final String ACTION_COUNT = "action_count";

  @Autowired private ActionFeedbackPublisher actionFeedbackPubl;

  @Autowired private ExportInfo exportInfo;

  @Autowired private ExportFileRepository exportFileRepository;

  @Publisher(channel = "sftpOutbound")
  public byte[] sendMessage(
      @Header(FileHeaders.REMOTE_FILE) String filename,
      @Header(RESPONSES_REQUIRED) String[] responseRequiredList,
      @Header(ACTION_COUNT) String actionCount,
      ByteArrayOutputStream stream) {
    log.info("sendMessage sftpOutbound");
    return stream.toByteArray();
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE);
    log.info("sftpSuccessProcess sftpSuccessProcess");
    Timestamp now = DateTimeUtil.nowUTC();

    int actionCount = Integer.valueOf((String) message.getPayload().getHeaders().get(ACTION_COUNT));

    ExportFile exportFile = exportFileRepository.findOneByFilename(filename);
    exportFile.setStatus(SendStatus.SUCCEEDED);
    exportFile.setDateSuccessfullySent(now);
    exportFile.setRowCount(actionCount);
    exportFileRepository.saveAndFlush(exportFile);

    String dateStr = new SimpleDateFormat(DATE_FORMAT).format(now);
    String[] responsesRequiredList =
        (String[]) message.getPayload().getHeaders().get(RESPONSES_REQUIRED);
    sendFeedbackMessage(responsesRequiredList, dateStr);
    log.debug("file_name:" + filename + ", Sftp transfer complete");
    exportInfo.addOutcome(filename + " transferred with " + actionCount + " requests.");
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(GenericMessage message) {
    log.info("sftpFailedProcess sftpFailedProcess");
    MessagingException payload = (MessagingException) message.getPayload();
    log.error("SFTP process failed", payload);
    MessageHeaders headers = payload.getFailedMessage().getHeaders();
    String fileName = (String) headers.get(FileHeaders.REMOTE_FILE);
    int actionCount = Integer.valueOf((String) headers.get(ACTION_COUNT));

    log.error("file_name:" + fileName + ", action_count: ", actionCount + ", Sftp transfer failed");

    ExportFile exportFile = exportFileRepository.findOneByFilename(fileName);
    exportFile.setStatus(SendStatus.FAILED);
    exportFile.setRowCount(actionCount);
    exportFileRepository.saveAndFlush(exportFile);

    exportInfo.addOutcome(fileName + " transfer failed for " + actionCount + " requests.");
  }

  /**
   * Send ActionFeedback
   *
   * @param actionIds of ActionRequests for which to send ActionFeedback.
   * @param dateStr when actioned.
   */
  private void sendFeedbackMessage(String[] actionIds, String dateStr) {
    for (String actionId : actionIds) {
      ActionFeedback actionFeedback =
          new ActionFeedback(actionId, "ActionExport Sent: " + dateStr, Outcome.REQUEST_COMPLETED);
      actionFeedbackPubl.sendActionFeedback(actionFeedback);
    }
  }
}
