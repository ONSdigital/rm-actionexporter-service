package uk.gov.ons.ctp.response.action.export.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ExportReport;

/** JPA repository for ExportReport entities */
@Repository
public interface ExportReportRepository extends JpaRepository<ExportReport, String> {}
