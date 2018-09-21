package uk.gov.ons.ctp.response.action.export.domain;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "actionrequest", schema = "actionexporter")
public class ExportJob {
  @Id
  private UUID id;

  @Column(name = "datesuccessfullysent")
  private Timestamp dateSuccessfullySent;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private JobStatus status;

  public enum JobStatus {
    INIT,
    QUEUED,
    SUCCEEDED,
    FAILED
  }
}
