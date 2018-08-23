package uk.gov.ons.ctp.response.action.export.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.net.URI;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.representation.TemplateMappingDTO;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

/** The REST endpoint controller for TemplateMappings. */
@RestController
@RequestMapping(value = "/templatemappings", produces = "application/json")
public class TemplateMappingEndpoint {
  private static final Logger log = LoggerFactory.getLogger(TemplateMappingEndpoint.class);

  @Autowired private TemplateMappingService templateMappingService;

  @Qualifier("actionExporterBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * To retrieve all TemplateMappings
   *
   * @return a list of TemplateMappings
   */
  @RequestMapping(method = RequestMethod.GET)
  public List<TemplateMappingDTO> findAllTemplateMappings() {
    log.debug("Entering findAllTemplateMappings ...");
    List<TemplateMapping> templateMappings = templateMappingService.retrieveAllTemplateMappings();
    List<TemplateMappingDTO> results =
        mapperFacade.mapAsList(templateMappings, TemplateMappingDTO.class);
    return CollectionUtils.isEmpty(results) ? null : results;
  }

  /**
   * To retrieve a specific TemplateMapping
   *
   * @param actionType for the specific TemplateMapping to retrieve
   * @return the specific TemplateMapping
   * @throws CTPException if no TemplateMapping found
   */
  @RequestMapping(value = "/{actionType}", method = RequestMethod.GET)
  public TemplateMappingDTO findTemplateMapping(@PathVariable("actionType") final String actionType)
      throws CTPException {
    log.with("action_type", actionType).debug("Entering findTemplateMapping");
    TemplateMapping result = templateMappingService.retrieveTemplateMappingByActionType(actionType);
    if (result == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          "Template Mapping not found for action type %s",
          actionType);
    }
    return mapperFacade.map(result, TemplateMappingDTO.class);
  }

  /**
   * To store TemplateMappings
   *
   * @param actionType the Action Type
   * @param templateMappingDTOList the TemplateMapping content
   * @return 201 if created
   * @throws CTPException if the TemplateMapping can't be stored
   */
  @RequestMapping(
      value = "/{actionType}",
      method = RequestMethod.POST,
      consumes = "application/json")
  public ResponseEntity<List<TemplateMappingDTO>> storeTemplateMappings(
      @PathVariable("actionType") final String actionType,
      @RequestBody final List<TemplateMappingDTO> templateMappingDTOList)
      throws CTPException {
    log.debug("Entering storeTemplateMapping");

    List<TemplateMapping> templateMappings =
        mapperFacade.mapAsList(templateMappingDTOList, TemplateMapping.class);

    if (templateMappings.isEmpty()) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "Template Mappings not created for action type %s",
          actionType);
    }

    List<TemplateMapping> mappings =
        templateMappingService.storeTemplateMappings(actionType, templateMappings);

    String newResourceUrl =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .buildAndExpand(actionType)
            .toUri()
            .toString();

    return ResponseEntity.created(URI.create(newResourceUrl))
        .body(mapperFacade.mapAsList(mappings, TemplateMappingDTO.class));
  }
}
