package uk.gov.ons.ctp.response.action.export.domain;

import java.util.UUID;
import javax.persistence.Entity;
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
@Table(name = "exportjob", schema = "actionexporter")
public class ExportJob {
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  private UUID id;
}
