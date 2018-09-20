package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;
import uk.gov.ons.ctp.response.action.export.domain.SendState;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
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
  private static final String ACTION_LIST = "list_actionIds";
  private static final int BATCH_SIZE = 10000;

  @Autowired private ActionRequestRepository actionRequestRepository;

  @Autowired private ExportReportRepository exportReportRepository;

  @Autowired private ActionFeedbackPublisher actionFeedbackPubl;

  @Autowired private ExportInfo exportInfo;

  @Publisher(channel = "sftpOutbound")
  public byte[] sendMessage(
      @Header(FileHeaders.REMOTE_FILE) String filename,
      @Header(ACTION_LIST) List<String> actionIds,
      ByteArrayOutputStream stream) {

    return stream.toByteArray();
  }

  /**
   * Using JPA entities to update repository for actionIds exported was slow. JPQL queries used for
   * performance reasons. To increase performance updates batched with IN clause.
   *
   * @param message Spring integration message sent
   */
  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    Timestamp now = DateTimeUtil.nowUTC();
    String dateStr = new SimpleDateFormat(DATE_FORMAT).format(now);

    List<String> actionList = (List<String>) message.getPayload().getHeaders().get(ACTION_LIST);
    List<List<String>> subLists = Lists.partition(actionList, BATCH_SIZE);
    Set<UUID> actionIds = new HashSet<>();
    subLists.forEach(
        (batch) -> {
          batch.forEach(
              (actionId) -> {
                actionIds.add(UUID.fromString(actionId));
              });
          int saved =
              actionRequestRepository.updateDateSentAndSendStateByActionId(
                  actionIds, now, SendState.SENT);
          if (actionIds.size() == saved) {
            sendFeedbackMessage(
                actionRequestRepository.retrieveResponseRequiredByActionId(actionIds), dateStr);
          } else {
            log.with("action_ids", actionIds).error("ActionRequests failed to update DateSent");
          }
          actionIds.clear();
        });

    ExportReport exportReport =
        new ExportReport(
            (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE),
            actionList.size(),
            now,
            true,
            false);
    exportReportRepository.save(exportReport);

    log.with("file_name", message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE))
        .debug("Sftp transfer complete");
    exportInfo.addOutcome(
        (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE)
            + " transferred with "
            + Integer.toString(actionList.size())
            + " requests.");
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(ErrorMessage message) {
    MessageHeaders headers =
        ((MessagingException) message.getPayload()).getFailedMessage().getHeaders();
    String fileName = (String) headers.get(FileHeaders.REMOTE_FILE);
    List<String> actionList = (List<String>) headers.get(ACTION_LIST);
    log.with("file_name", fileName)
        .with("action_requests", actionList)
        .with("payload", message.getPayload())
        .error("Sftp transfer failed");

    List<List<String>> subLists = Lists.partition(actionList, BATCH_SIZE);
    Set<UUID> actionIds = new HashSet<>();
    subLists.forEach(
        (batch) -> {
          batch.forEach(
              (actionId) -> {
                actionIds.add(UUID.fromString(actionId));
              });
          int saved =
              actionRequestRepository.updateSendStateByActionId(actionIds, SendState.FAILED);

          if (actionIds.size() == saved) {
            log.with("action_ids", actionIds).error("ActionRequests failed to mark as FAILED");
          }
        });

    exportInfo.addOutcome(
        fileName + " transfer failed with " + Integer.toString(actionList.size()) + " requests.");
    ExportReport exportReport =
        new ExportReport(fileName, actionList.size(), DateTimeUtil.nowUTC(), false, false);
    exportReportRepository.save(exportReport);
  }

  /**
   * Send ActionFeedback
   *
   * @param actionIds of ActionRequests for which to send ActionFeedback.
   * @param dateStr when actioned.
   */
  private void sendFeedbackMessage(List<UUID> actionIds, String dateStr) {
    actionIds.forEach(
        (actionId) -> {
          ActionFeedback actionFeedback =
              new ActionFeedback(
                  actionId.toString(), "ActionExport Sent: " + dateStr, Outcome.REQUEST_COMPLETED);
          actionFeedbackPubl.sendActionFeedback(actionFeedback);
        });
  }
}
