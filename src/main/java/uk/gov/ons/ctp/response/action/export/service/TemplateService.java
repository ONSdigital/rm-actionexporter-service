package uk.gov.ons.ctp.response.action.export.service;

import static uk.gov.ons.ctp.common.util.InputStreamUtils.getStringFromInputStream;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.TemplateExpression;
import uk.gov.ons.ctp.response.action.export.repository.TemplateRepository;

/**
 * The implementation of the TemplateService TODO Specific to FreeMarker at the moment with
 * freemarker.template.Configuration, clearTemplateCache, etc.
 */
@Service
public class TemplateService {
  private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

  public static final String ERROR_RETRIEVING_FREEMARKER_TEMPLATE =
      "Could not find FreeMarker template.";
  public static final String EXCEPTION_STORE_TEMPLATE =
      "Issue storing template. It appears to be empty.";

  @Autowired private TemplateRepository repository;

  @Autowired private freemarker.template.Configuration configuration;

  public TemplateExpression retrieveTemplate(String templateName) {
    return repository.findOne(templateName);
  }

  public List<TemplateExpression> retrieveAllTemplates() {
    return repository.findAll();
  }

  public TemplateExpression storeTemplate(String templateName, InputStream fileContents)
      throws CTPException {
    String stringValue = getStringFromInputStream(fileContents);
    if (StringUtils.isEmpty(stringValue)) {
      log.error(EXCEPTION_STORE_TEMPLATE);
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, EXCEPTION_STORE_TEMPLATE);
    }

    TemplateExpression template = new TemplateExpression();
    template.setContent(stringValue);
    template.setName(templateName);
    template.setDateModified(new Date());
    template = repository.save(template);

    configuration.clearTemplateCache();

    return template;
  }

  public File file(
      List<ActionRequestInstruction> actionRequestList, String templateName, String path)
      throws CTPException {
    File resultFile = new File(path);
    Writer fileWriter = null;
    try {
      Template template = giveTemplate(templateName);
      fileWriter = new FileWriter(resultFile);
      template.process(buildDataModel(actionRequestList), fileWriter);
    } catch (IOException | TemplateException e) {
      log.error("Exception thrown while templating for file...", e);
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
    } finally {
      if (fileWriter != null) {
        try {
          fileWriter.close();
        } catch (IOException e) {
          log.error("IOException thrown while closing the file writer...", e);
        }
      }
    }

    return resultFile;
  }

  public ByteArrayOutputStream stream(
      List<ActionRequestInstruction> actionRequestList, String templateName) throws CTPException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer outputStreamWriter = null;
    try {
      Template template = giveTemplate(templateName);
      outputStreamWriter = new OutputStreamWriter(outputStream);
      template.process(buildDataModel(actionRequestList), outputStreamWriter);
      outputStreamWriter.close();
    } catch (IOException | TemplateException e) {
      log.error("Exception thrown while templating for stream...", e);
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
    } finally {
      if (outputStreamWriter != null) {
        try {
          outputStreamWriter.close();
        } catch (IOException e) {
          log.error("IOException thrown while closing the output stream writer...", e);
        }
      }
    }

    return outputStream;
  }

  /**
   * This returns the FreeMarker template required for the transformation.
   *
   * @param templateName the FreeMarker template to use
   * @return the FreeMarker template
   * @throws IOException if issue creating the FreeMarker template
   * @throws CTPException if problem getting Freemarker template with name given
   */
  private Template giveTemplate(String templateName) throws CTPException, IOException {
    log.with("template_name", templateName).debug("Entering giveMeTemplate");
    Template template = configuration.getTemplate(templateName);

    log.debug("Received template");

    if (template == null) {
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, ERROR_RETRIEVING_FREEMARKER_TEMPLATE);
    }
    return template;
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
