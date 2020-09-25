package uk.gov.ons.ctp.response.action.export.service;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;
import uk.gov.ons.ctp.response.action.export.domain.ExportJob;
import uk.gov.ons.ctp.response.action.export.message.EventPublisher;
import uk.gov.ons.ctp.response.action.export.repository.ExportFileRepository;

@Service
public class NotificationFileCreator {

  private static final Logger log = LoggerFactory.getLogger(NotificationFileCreator.class);

  private static final SimpleDateFormat FILENAME_DATE_FORMAT =
      new SimpleDateFormat("ddMMyyyy_HHmm");

  private final EventPublisher eventPublisher;

  private final ExportFileRepository exportFileRepository;

  private final Clock clock;

  private PrintFileService printFileService;

  public NotificationFileCreator(
      EventPublisher eventPublisher,
      ExportFileRepository exportFileRepository,
      Clock clock,
      AppConfig appConfig,
      PrintFileService printFileService) {
    this.eventPublisher = eventPublisher;
    this.exportFileRepository = exportFileRepository;
    this.clock = clock;
    this.printFileService = printFileService;
  }

  public void uploadData(
      String filenamePrefix,
      List<ActionRequestInstruction> actionRequestInstructions,
      ExportJob exportJob) {
    if (actionRequestInstructions.isEmpty()) {
      log.info("no action request instructions to export");
      return;
    }

    final String now = FILENAME_DATE_FORMAT.format(clock.millis());
    String filename = String.format("%s_%s.csv", filenamePrefix, now);

    if (exportFileRepository.existsByFilename(filename)) {
      log.warn(
          "filename: "
              + filename
              + ", duplicate filename. The cron job is probably running too frequently. The "
              + "Action Exporter service is designed to only run every minute, maximum");
      throw new RuntimeException();
    }

    log.info("filename: " + filename + ", uploading file");

    ExportFile exportFile = new ExportFile();
    exportFile.setExportJobId(exportJob.getId());
    exportFile.setFilename(filename);
    exportFileRepository.saveAndFlush(exportFile);

    // temporarily hook in here as at this point we know the name of the file
    // and all the action request instructions
    printFileService.send(filename, actionRequestInstructions);

    eventPublisher.publishEvent("Printed file " + filename);
  }
}
