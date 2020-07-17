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

/** Domain entity representing a template mapping. */
@Entity
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "templatemapping", schema = "actionexporter")
public class TemplateMapping {

  @Id
  @Column(name = "actiontypenamepk")
  private String actionType;

  @Column(name = "templatename")
  private String template;

  @Column(name = "filenameprefix")
  private String fileNamePrefix;

  @Column(name = "datemodified")
  private Date dateModified;
}
