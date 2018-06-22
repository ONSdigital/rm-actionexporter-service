package uk.gov.ons.ctp.response.action.export.representation;

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

/** Representation of a Template */
@CoverageIgnore
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class TemplateExpressionDTO {
  @NotNull private String name;
  private String content;
  private Date dateModified;
}
