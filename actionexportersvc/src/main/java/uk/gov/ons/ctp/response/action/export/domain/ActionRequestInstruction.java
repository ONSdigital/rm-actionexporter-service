package uk.gov.ons.ctp.response.action.export.domain;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "actionrequest", schema = "actionexporter")
public class ActionRequestInstruction {

  @Id
  @Column(name = "actionid")
  private UUID actionId;

  @Column(name = "responserequired")
  private boolean responseRequired;

  @Column(name = "actionplanname")
  private String actionPlan;

  @Column(name = "actiontypename")
  private String actionType;

  @Column(name = "questionset")
  private String questionSet;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "contactfk", referencedColumnName = "contactpk")
  private Contact contact;

  @ManyToOne
  @JoinColumn(name = "sampleunitreffk", referencedColumnName = "sampleunitrefpk")
  private Address address;

  @Column(name = "caseid")
  private UUID caseId;

  @Enumerated(EnumType.STRING)
  private Priority priority;

  @Column(name = "caseref")
  private String caseRef;

  private String iac;

  @Column(name = "datestored")
  private Timestamp dateStored;

  @Column(name = "datesent")
  private Timestamp dateSent;

}
