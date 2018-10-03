package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Publisher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile.SendStatus;
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportReportRepository;
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

  @Autowired private ExportReportRepository exportReportRepository;

  @Autowired private ActionFeedbackPublisher actionFeedbackPubl;

  @Autowired private ExportInfo exportInfo;

  @Autowired private ExportFileRepository exportFileRepository;

  @Publisher(channel = "sftpOutbound")
  public byte[] sendMessage(
      @Header(FileHeaders.REMOTE_FILE) String filename,
      @Header(RESPONSES_REQUIRED) String[] responseRequiredList,
      @Header(ACTION_COUNT) String actionCount,
      ByteArrayOutputStream stream) {

    return stream.toByteArray();
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    String filename = (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE);
    Timestamp now = DateTimeUtil.nowUTC();

    ExportFile exportFile = exportFileRepository.findOneByFilename(filename);
    exportFile.setStatus(SendStatus.SUCCEEDED);
    exportFile.setDateSuccessfullySent(now);
    exportFileRepository.saveAndFlush(exportFile);

    String dateStr = new SimpleDateFormat(DATE_FORMAT).format(now);
    String[] responsesRequiredList =
        (String[]) message.getPayload().getHeaders().get(RESPONSES_REQUIRED);
    sendFeedbackMessage(responsesRequiredList, dateStr);

    int actionCount = Integer.valueOf((String) message.getPayload().getHeaders().get(ACTION_COUNT));
    ExportReport exportReport = new ExportReport(filename, actionCount, now, true, false);
    exportReportRepository.save(exportReport);

    log.with("file_name", filename).debug("Sftp transfer complete");
    exportInfo.addOutcome(filename + " transferred with " + actionCount + " requests.");
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(ErrorMessage message) {
    MessageHeaders headers =
        ((MessagingException) message.getPayload()).getFailedMessage().getHeaders();
    String fileName = (String) headers.get(FileHeaders.REMOTE_FILE);
    int actionCount = Integer.valueOf((String) headers.get(ACTION_COUNT));

    log.with("file_name", fileName)
        .with("action_count", actionCount)
        .with("payload", message.getPayload())
        .error("Sftp transfer failed");

    ExportFile exportFile = exportFileRepository.findOneByFilename(fileName);
    exportFile.setStatus(SendStatus.FAILED);
    exportFileRepository.saveAndFlush(exportFile);

    exportInfo.addOutcome(fileName + " transfer failed for " + actionCount + " requests.");
    ExportReport exportReport =
        new ExportReport(fileName, actionCount, DateTimeUtil.nowUTC(), false, false);
    exportReportRepository.save(exportReport);
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
