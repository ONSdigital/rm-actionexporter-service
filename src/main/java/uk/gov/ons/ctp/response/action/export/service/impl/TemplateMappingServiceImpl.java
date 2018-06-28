package uk.gov.ons.ctp.response.action.export.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.TemplateMappingRepository;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

/** The implementation of the TemplateMappingService */
@Service
@Slf4j
public class TemplateMappingServiceImpl implements TemplateMappingService {

  public static final String EXCEPTION_STORE_TEMPLATE_MAPPING =
      "Issue storing TemplateMapping. Appears to be empty.";

  public static final String EXCEPTION_RETRIEVING_TEMPLATE_MAPPING = "TemplateMapping not found.";

  @Autowired private TemplateMappingRepository repository;

  @Override
  public List<TemplateMapping> storeTemplateMappings(
      String actionType, List<TemplateMapping> templateMappingList) throws CTPException {

    for (TemplateMapping templateMapping : templateMappingList) {
      templateMapping.setActionType(actionType);
      templateMapping.setDateModified(new Date());
      repository.save(templateMapping);
    }

    return templateMappingList;
  }

  @Override
  public TemplateMapping retrieveTemplateMappingByActionType(String actionType)
      throws CTPException {
    TemplateMapping templateMapping = repository.findOne(actionType);
    if (templateMapping == null) {
      log.error("No template mapping for actionType {} found.", actionType);
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format("%s %s", EXCEPTION_RETRIEVING_TEMPLATE_MAPPING, actionType));
    }
    return templateMapping;
  }

  @Override
  public List<TemplateMapping> retrieveAllTemplateMappings() {
    return repository.findAll();
  }

  @Override
  public Map<String, List<TemplateMapping>> retrieveAllTemplateMappingsByFilename() {
    return retrieveAllTemplateMappings()
        .stream()
        .collect(Collectors.groupingBy(TemplateMapping::getFileNamePrefix));
  }

  @Override
  public Map<String, TemplateMapping> retrieveAllTemplateMappingsByActionType() {
    Map<String, TemplateMapping> mappings = new HashMap<String, TemplateMapping>();
    retrieveAllTemplateMappings()
        .forEach(
            (templateMapping) -> {
              mappings.put(templateMapping.getActionType(), templateMapping);
            });
    return mappings;
  }

  @Override
  public List<String> retrieveActionTypes() {
    return repository.findAllActionType();
  }
}
