package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;

/**
 * The implementation of the TemplateService TODO Specific to FreeMarker at the moment with
 * freemarker.template.Configuration, clearTemplateCache, etc.
 */
@Service
public class TemplateService {
  private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

  public static final String ERROR_RETRIEVING_FREEMARKER_TEMPLATE =
      "Could not find FreeMarker template.";

  @Autowired private freemarker.template.Configuration configuration;

  public ByteArrayOutputStream stream(
      List<ActionRequestInstruction> actionRequestList, String templateName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (Writer outputStreamWriter = new OutputStreamWriter(outputStream)) {
      Template template = getTemplate(templateName);
      template.process(buildDataModel(actionRequestList), outputStreamWriter);
    } catch (IOException | TemplateException e) {
      log.error("Exception thrown while templating for stream...", e);
      throw new RuntimeException(e.getMessage());
    }

    return outputStream;
  }

  /**
   * This returns the FreeMarker template required for the transformation.
   *
   * @param templateName the FreeMarker template to use
   * @return the FreeMarker template
   */
  private Template getTemplate(String templateName) {
    log.with("template_name", templateName).debug("Entering giveMeTemplate");
    Template template = null;
    try {
      print(configuration);
      template = configuration.getTemplate(templateName);
    } catch (IOException e) {
      log.with(e).error("Error reading freemarker template").;
      throw new RuntimeException("Error reading freemarker template", e);
    }

    log.debug("Received template");

    if (template == null) {
      throw new IllegalStateException(ERROR_RETRIEVING_FREEMARKER_TEMPLATE);
    }
    return template;
  }
  private void print(freemarker.template.Configuration configuration) {
    log.with(configuration.getDefaultEncoding())
            .with(configuration.getTemplateUpdateDelayMilliseconds())
            .with(configuration.getVersion())
            .with(configuration.getLogTemplateExceptions())
            .with(configuration.getTemplateExceptionHandler())
            .error("Freemarker configuration");
  }

  /**
   * This builds the data model required by FreeMarker
   *
   * @param actionRequestList the list of action requests
   * @return the data model map
   */
  private Map<String, Object> buildDataModel(List<ActionRequestInstruction> actionRequestList) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("actionRequests", actionRequestList);
    return result;
  }
}
