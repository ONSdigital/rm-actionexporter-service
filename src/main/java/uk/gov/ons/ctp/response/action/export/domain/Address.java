package uk.gov.ons.ctp.response.action.export.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "address", schema = "actionexporter")
public class Address {

  @Id
  @Column(name = "sampleunitrefpk")
  private String sampleUnitRef;

  @Column(name = "addresstype")
  private String addressType;

  @Column(name = "estabtype")
  private String estabType;

  private String category;

  private String organisationName;

  @Column(name = "address_line1")
  private String line1;

  @Column(name = "address_line2")
  private String line2;

  private String locality;

  private String townName;

  private String postcode;

  @Column(name = "lad")
  private String ladCode;

  private BigDecimal latitude;

  private BigDecimal longitude;

}
