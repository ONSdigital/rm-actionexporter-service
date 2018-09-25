package uk.gov.ons.ctp.response.action.export.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ExportFile;

@Repository
public interface ExportFileRepository extends JpaRepository<ExportFile, UUID> {
  ExportFile findOneByFilename(String filename);
}
