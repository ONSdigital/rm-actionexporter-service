package uk.gov.ons.ctp.response.action.export.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;

import javax.transaction.Transactional;

/**
 * JPA repository for ExportReport entities
 */
@Repository
public interface ExportReportRepository extends JpaRepository<ExportReport, String> {

  /**
   * exports generated reports.
   * @return boolean whether report has been created successfully
   */
  @Modifying
  @Transactional
  @Procedure(name = "createReport")
  boolean createReport();

}
