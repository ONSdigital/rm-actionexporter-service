package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.TemplateMappingRepository;

/** The implementation of the TemplateMappingService */
@Service
public class TemplateMappingService {
  private static final Logger log = LoggerFactory.getLogger(TemplateMappingService.class);

  public static final String EXCEPTION_STORE_TEMPLATE_MAPPING =
      "Issue storing TemplateMapping. Appears to be empty.";

  public static final String EXCEPTION_RETRIEVING_TEMPLATE_MAPPING = "TemplateMapping not found.";

  @Autowired private TemplateMappingRepository repository;

  public List<TemplateMapping> storeTemplateMappings(
      String actionType, List<TemplateMapping> templateMappingList) {

    for (TemplateMapping templateMapping : templateMappingList) {
      templateMapping.setActionType(actionType);
      templateMapping.setDateModified(new Date());
      repository.save(templateMapping);
    }

    return templateMappingList;
  }

  public TemplateMapping retrieveTemplateMappingByActionType(String actionType)
      throws CTPException {
    TemplateMapping templateMapping = repository.findOne(actionType);
    if (templateMapping == null) {
      log.with("action_type", actionType).error("No template mapping for actionType found.");
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", EXCEPTION_RETRIEVING_TEMPLATE_MAPPING, actionType));
    }
    return templateMapping;
  }

  public List<TemplateMapping> retrieveAllTemplateMappings() {
    return repository.findAll();
  }

  public Map<String, List<TemplateMapping>> retrieveAllTemplateMappingsByFilename() {
    return retrieveAllTemplateMappings()
        .stream()
        .collect(Collectors.groupingBy(TemplateMapping::getFileNamePrefix));
  }
}
