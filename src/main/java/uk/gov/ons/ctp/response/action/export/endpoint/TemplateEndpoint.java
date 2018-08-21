package uk.gov.ons.ctp.response.action.export.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateExpression;
import uk.gov.ons.ctp.response.action.export.representation.TemplateExpressionDTO;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;

/** The REST endpoint controller for Templates. */
@RestController
@RequestMapping(value = "/templates", produces = "application/json")
public class TemplateEndpoint {
  private static final Logger log = LoggerFactory.getLogger(TemplateEndpoint.class);

  @Autowired private TemplateService templateService;

  @Qualifier("actionExporterBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * To retrieve all Templates
   *
   * @return a list of Templates
   */
  @RequestMapping(method = RequestMethod.GET)
  public List<TemplateExpressionDTO> findAllTemplates() {
    log.debug("Entering findAllTemplates ...");
    List<TemplateExpression> templates = templateService.retrieveAllTemplates();
    List<TemplateExpressionDTO> results =
        mapperFacade.mapAsList(templates, TemplateExpressionDTO.class);
    return CollectionUtils.isEmpty(results) ? null : results;
  }

  /**
   * To retrieve a specific Template
   *
   * @param templateName for the specific Template to retrieve
   * @return the specific Template
   * @throws CTPException if no Template found
   */
  @RequestMapping(value = "/{templateName}", method = RequestMethod.GET)
  public TemplateExpressionDTO findTemplate(@PathVariable("templateName") final String templateName)
      throws CTPException {
    log.debug("Entering findTemplate with {}", templateName);
    TemplateExpression result = templateService.retrieveTemplate(templateName);
    if (result == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND, "Template not found for name %s", templateName);
    }

    return mapperFacade.map(result, TemplateExpressionDTO.class);
  }

  /**
   * To store a Template
   *
   * @param templateName the Template name
   * @param file the Template content
   * @return 201 if created
   * @throws CTPException if the Template can't be stored
   */
  @RequestMapping(
      value = "/{templateName}",
      method = RequestMethod.POST,
      consumes = "multipart/form-data")
  public ResponseEntity<TemplateExpressionDTO> storeTemplate(
      @PathVariable("templateName") final String templateName,
      @RequestParam("file") MultipartFile file)
      throws CTPException {
    log.debug("Entering storeTemplate with templateName {}", templateName);
    try {
      TemplateExpression template =
          templateService.storeTemplate(templateName, file.getInputStream());

      TemplateExpressionDTO templateDTO = mapperFacade.map(template, TemplateExpressionDTO.class);

      String newResourceUrl =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .buildAndExpand(templateName)
              .toUri()
              .toString();

      return ResponseEntity.created(URI.create(newResourceUrl)).body(templateDTO);
    } catch (IOException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Failed reading the provided template file.");
    }
  }
}
