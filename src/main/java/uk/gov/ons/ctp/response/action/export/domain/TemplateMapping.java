package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Domain entity representing a template mapping.
 */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "templatemapping", schema = "actionexporter")
public class TemplateMapping {

  @Id
  @Column(name = "actiontypenamepk")
  private String actionType;

  @Column(name = "templatenamefk")
  private String template;

  private String file;

  @Column(name = "datemodified")
  private Date dateModified;

}
