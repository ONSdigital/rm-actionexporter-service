package uk.gov.ons.ctp.response.action.export;

import java.util.Collections;
import java.util.List;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;

public class TemplateMappings {

  public static List<TemplateMapping> templateMappingsWithActionType(String bsnot) {
    TemplateMapping mapping = new TemplateMapping();
    mapping.setActionType("BSNOT");
    return Collections.singletonList(mapping);
  }
}
