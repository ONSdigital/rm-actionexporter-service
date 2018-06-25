package uk.gov.ons.ctp.response.action.export.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contact", schema = "actionexporter")
public class Contact {

  @Id
  @GenericGenerator(
      name = "actionexportseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "actionexporter.contactpkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actionexportseq_gen")
  @Column(name = "contactpk")
  private Integer contactPk;

  private String title;

  private String forename;

  private String surname;

  @Column(name = "phonenumber")
  private String phoneNumber;

  @Column(name = "emailaddress")
  private String emailAddress;
}
