package uk.gov.ons.ctp.response.action.export.representation;

import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.data.annotation.Id;

/** Representation of an ActionRequest */
@CoverageIgnore
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionRequestInstructionDTO {
  @Id private UUID actionId;
  private boolean responseRequired;
  private String actionPlan;
  private String actionType;
  private String questionSet;
  // private ActionContact contact;
  // private ActionAddress address;
  private UUID caseId;
  // private Priority priority;
  private String caseRef;
  private String iac;
  // private ActionEvent events;
  private Date dateStored;
  private Date dateSent;
  private Date returnBy;
  private String surveyAbbreviation;
}
