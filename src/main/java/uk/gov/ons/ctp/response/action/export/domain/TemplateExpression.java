package uk.gov.ons.ctp.response.action.export.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

/** Domain entity representing a Template. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "template", schema = "actionexporter")
public class TemplateExpression {

  @Id
  @Column(name = "templatenamepk")
  private String name;

  private String content;

  @Column(name = "datemodified")
  private Date dateModified;
}
