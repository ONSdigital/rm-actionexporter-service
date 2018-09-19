package uk.gov.ons.ctp.response.action.export.domain;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "actionrequest", schema = "actionexporter")
public class ActionRequestInstruction {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actionexportseq_gen")
  @GenericGenerator(
      name = "actionexportseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "actionexporter.actionrequestpkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "actionrequestpk")
  private Integer actionrequestPK;

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

  @Column(name = "legalbasis")
  private String legalBasis;

  @Column(name = "region")
  private String region;

  @Column(name = "respondentstatus")
  private String respondentStatus;

  @Column(name = "enrolmentstatus")
  private String enrolmentStatus;

  @Column(name = "casegroupstatus")
  private String caseGroupStatus;

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

  @Column(name = "surveyref")
  private String surveyRef;

  @Column(name = "exerciseref")
  private String exerciseRef;

  @Column(name = "returnbydate")
  private String returnByDate;

  @Column(name = "sendstate")
  @Enumerated(EnumType.STRING)
  private SendState sendState;
}
