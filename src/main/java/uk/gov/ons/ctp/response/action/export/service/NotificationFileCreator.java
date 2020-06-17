package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.export.message.UploadObjectGCS;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@Service
public class NotificationFileCreator {

  private static final Logger log = LoggerFactory.getLogger(NotificationFileCreator.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  private final SftpServicePublisher sftpService;

  private final EventPublisher eventPublisher;

  private final ExportFileRepository exportFileRepository;

  private final Clock clock;

  private final UploadObjectGCS uploadObjectGCS;

  private final AppConfig appConfig;

  public NotificationFileCreator(
      SftpServicePublisher sftpService,
      EventPublisher eventPublisher,
      ExportFileRepository exportFileRepository,
      Clock clock,
      UploadObjectGCS uploadObjectGCS,
      AppConfig appConfig) {
    this.sftpService = sftpService;
    this.eventPublisher = eventPublisher;
    this.exportFileRepository = exportFileRepository;
    this.clock = clock;
    this.uploadObjectGCS = uploadObjectGCS;
    this.appConfig = appConfig;
  }

  public void uploadData(
      String filenamePrefix,
      ByteArrayOutputStream data,
      UUID exportJob,
      String[] responseRequiredList,
      int actionCount) {
    if (actionCount == 0) {
      return;
    }

    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String filename = String.format("%s_%s.csv", filenamePrefix, now);

    if (exportFileRepository.existsByFilename(filename)) {
      log.with("filename", filename)
          .warn(
              "Duplicate filename. The cron job is probably running too frequently. The "
                  + "Action Exporter service is designed to only run every minute, maximum");
      throw new RuntimeException();
    }

    log.with("filename", filename).info("Uploading file");

    ExportFile exportFile = new ExportFile();
    exportFile.setExportJobId(exportJob);
    exportFile.setFilename(filename);
    exportFileRepository.saveAndFlush(exportFile);

    boolean isEnabled = appConfig.getGcs().isEnabled();
    if (isEnabled) {
      String bucket = appConfig.getGcs().getBucket();
      uploadObjectGCS.uploadObject(filename, bucket, data);
      log.with("bucket", bucket).info("File Uploaded to bucket.");
    }
    sftpService.sendMessage(filename, responseRequiredList, Integer.toString(actionCount), data);

    eventPublisher.publishEvent("Printed file " + filename);
  }
}
