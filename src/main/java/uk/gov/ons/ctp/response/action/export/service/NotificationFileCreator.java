package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;

@Service
public class NotificationFileCreator {

  private static final Logger log = LoggerFactory.getLogger(NotificationFileCreator.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  private final SftpServicePublisher sftpService;

  private final EventPublisher eventPublisher;

  private final Clock clock;

  public NotificationFileCreator(
      SftpServicePublisher sftpService,
      EventPublisher eventPublisher,
      Clock clock) {
    this.sftpService = sftpService;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }


  public void uploadData(String filenamePrefix, ByteArrayOutputStream data, ExportJob exportJob) {
    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String filename = String.format("%s_%s.csv", filenamePrefix, now);
    if (data.size() == 0) {
      return;
    }
    log.with("filename", filename).info("Uploading file");
    sftpService.sendMessage(
        filename,
        exportJob.getId().toString(),
        data);
    eventPublisher.publishEvent("Printed file " + filename);
  }
}
