package uk.gov.ons.ctp.response.action.export.message.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.ons.ctp.response.action.export.message.ActionFeedbackPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.scheduled.ExportInfo;
import uk.gov.ons.ctp.response.action.export.service.ActionRequestService;
import uk.gov.ons.ctp.response.action.export.service.ExportReportService;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service implementation responsible for publishing transformed ActionRequests
 * via sftp. See Spring Integration flow for details of sftp outbound channel.
 *
 */
@MessageEndpoint
@Slf4j
public class SftpServicePublisherImpl implements SftpServicePublisher {

  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
  private static final String ACTION_LIST = "list_actionIds";
  private static final int BATCH_SIZE = 10000;

  @Autowired
  private ActionRequestService actionRequestService;

  @Autowired
  private ExportReportService exportReportService;

  @Autowired
  private ActionFeedbackPublisher actionFeedbackPubl;

  @Autowired
  private ExportInfo exportInfo;
  
  @Override
  @Publisher(channel = "sftpOutbound")
  public byte[] sendMessage(@Header(FileHeaders.REMOTE_FILE) String filename,
      @Header(ACTION_LIST) List<String> actionIds, ByteArrayOutputStream stream) {
    
    return stream.toByteArray();
  }

  /**
   * Using JPA entities to update repository for actionIds exported was slow.
   * JPQL queries used for performance reasons. To increase performance updates
   * batched with IN clause.
   *
   * @param message Spring integration message sent
   */
  @SuppressWarnings("unchecked")
  @Override
  @ServiceActivator(inputChannel = "sftpSuccessProcess")
  public void sftpSuccessProcess(GenericMessage<GenericMessage<byte[]>> message) {
    Timestamp now = DateTimeUtil.nowUTC();
    String dateStr = new SimpleDateFormat(DATE_FORMAT).format(now);

    List<String> actionList = (List<String>) message.getPayload().getHeaders().get(ACTION_LIST);
    List<List<String>> subLists = Lists.partition(actionList, BATCH_SIZE);
    Set<UUID> actionIds = new HashSet<UUID>();
    subLists.forEach((batch) -> {
      batch.forEach((actionId) -> {
        actionIds.add(UUID.fromString(actionId));
      });
      int saved = actionRequestService.updateDateSentByActionId(actionIds, now);
      if (actionIds.size() == saved) {
        sendFeedbackMessage(actionRequestService.retrieveResponseRequiredByActionId(actionIds), dateStr);
      } else {
        log.error("ActionRequests {} failed to update DateSent", actionIds);
      }
      actionIds.clear();
    });

    ExportReport exportReport = new ExportReport(
        (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE), actionList.size(), now, true, false);
    exportReportService.save(exportReport);

    log.info("Sftp transfer complete for file {}", message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE));
    exportInfo.addOutcome((String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE) + " transferred with "
        + Integer.toString(actionList.size()) + " requests.");
  }

  @SuppressWarnings("unchecked")
  @Override
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(ErrorMessage message) {
    MessageHeaders headers = ((MessagingException) message.getPayload()).getFailedMessage().getHeaders();
    String fileName = (String) headers.get(FileHeaders.REMOTE_FILE);
    List<String> actionList = (List<String>) headers.get(ACTION_LIST);
    log.error("Sftp transfer failed for file {} for action requests {}", fileName, actionList, message.getPayload());
    exportInfo.addOutcome(fileName + " transfer failed with " + Integer.toString(actionList.size()) + " requests.");
    ExportReport exportReport = new ExportReport(fileName, actionList.size(), DateTimeUtil.nowUTC(), false, false);
    exportReportService.save(exportReport);
  }

  /**
   * Send ActionFeedback
   *
   * @param actionIds of ActionRequests for which to send ActionFeedback.
   * @param dateStr when actioned.
   */
  private void sendFeedbackMessage(List<UUID> actionIds, String dateStr) {
    actionIds.forEach((actionId) -> {
      ActionFeedback actionFeedback = new ActionFeedback(actionId.toString(),
          "ActionExport Sent: " + dateStr, Outcome.REQUEST_COMPLETED);
      actionFeedbackPubl.sendActionFeedback(actionFeedback);
    });
  }

}
