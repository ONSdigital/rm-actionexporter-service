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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exportfile", schema = "actionexporter")
public class ExportFile {
  @Id private UUID id = UUID.randomUUID();

  @Column(name = "filename")
  private String filename;

  @Column(name = "exportjobid")
  private UUID exportJobId;

  @Column(name = "datesuccessfullysent")
  private Timestamp dateSuccessfullySent;

  @Column(name = "rowcount")
  private int rowCount;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SendStatus status = SendStatus.QUEUED;

  public enum SendStatus {
    QUEUED,
    SUCCEEDED,
    FAILED
  }
}
