package uk.gov.ons.ctp.response.action.export.domain;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;

@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "exportfile", schema = "actionexporter")
public class ExportFile {
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  private UUID id;

  @Column(name = "filename")
  private String filename;

  @Column(name = "exportjobid")
  private UUID exportJobId;

  @Column(name = "datesuccessfullysent")
  private Timestamp dateSuccessfullySent;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SendStatus status = SendStatus.QUEUED;

  public enum SendStatus {
    QUEUED,
    SUCCEEDED,
    FAILED
  }
}
