package uk.gov.ons.ctp.response.action.export.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
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
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob.JobStatus;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.ExportJobRepository;
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
  private static final String JOB_ID = "export_job_id";

  @Autowired private ActionRequestRepository actionRequestRepository;

  @Autowired private ExportReportRepository exportReportRepository;

  @Autowired private ActionFeedbackPublisher actionFeedbackPubl;

  @Autowired private ExportInfo exportInfo;

  @Autowired private ExportJobRepository exportJobRepository;

  @Publisher(channel = "sftpOutbound")
  public byte[] sendMessage(
      @Header(FileHeaders.REMOTE_FILE) String filename,
      @Header(JOB_ID) String exportJobId,
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
    UUID exportJobId = UUID.fromString((String)message.getHeaders().get(JOB_ID));

    ExportJob exportJob = exportJobRepository.findOne(exportJobId);
    exportJob.setStatus(JobStatus.SUCCEEDED);
    exportJob.setDateSuccessfullySent(now);
    exportJobRepository.saveAndFlush(exportJob);

    sendFeedbackMessage(actionRequestRepository.retrieveResponseRequiredForJob(exportJobId),
        dateStr);

    int actionRequestInstructionCount = actionRequestRepository.countByExportJobId(exportJobId);

    ExportReport exportReport =
        new ExportReport(
            (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE),
            actionRequestInstructionCount,
            now,
            true,
            false);
    exportReportRepository.save(exportReport);

    log.with("file_name", message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE))
        .debug("Sftp transfer complete");
    exportInfo.addOutcome(
        (String) message.getPayload().getHeaders().get(FileHeaders.REMOTE_FILE)
            + " transferred with "
            + Integer.toString(actionRequestInstructionCount)
            + " requests.");
  }

  @SuppressWarnings("unchecked")
  @ServiceActivator(inputChannel = "sftpFailedProcess")
  public void sftpFailedProcess(ErrorMessage message) {
    MessageHeaders headers =
        ((MessagingException) message.getPayload()).getFailedMessage().getHeaders();
    String fileName = (String) headers.get(FileHeaders.REMOTE_FILE);
    UUID exportJobId = UUID.fromString((String)headers.get(JOB_ID));
    log.with("file_name", fileName)
        .with("export_job_id", exportJobId)
        .with("payload", message.getPayload())
        .error("Sftp transfer failed");

    ExportJob exportJob = exportJobRepository.findOne(exportJobId);
    exportJob.setStatus(JobStatus.FAILED);
    exportJobRepository.saveAndFlush(exportJob);

    int actionRequestInstructionCount = actionRequestRepository.countByExportJobId(exportJobId);

    exportInfo.addOutcome(
        fileName + " transfer failed for " + actionRequestInstructionCount + "action requests");
    ExportReport exportReport =
        new ExportReport(fileName, actionRequestInstructionCount, DateTimeUtil.nowUTC(), false, false);
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
