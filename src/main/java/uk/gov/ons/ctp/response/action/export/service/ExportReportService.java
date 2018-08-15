package uk.gov.ons.ctp.response.action.export.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;
import uk.gov.ons.ctp.response.action.export.repository.ExportReportRepository;

/** The implementation of FileRowCountService */
@Service
public class ExportReportService {

  @Autowired private ExportReportRepository exportReportRepo;

  public ExportReport save(ExportReport exportReport) {
    return exportReportRepo.save(exportReport);
  }

  public boolean createReport() {
    return exportReportRepo.createReport();
  }
}
