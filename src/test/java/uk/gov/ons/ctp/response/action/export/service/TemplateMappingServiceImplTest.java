package uk.gov.ons.ctp.response.action.export.service;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.TemplateMappingRepository;

/** To unit test TemplateMappingServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class TemplateMappingServiceImplTest {

  @InjectMocks private TemplateMappingService templateMappingService;

  @Mock private TemplateMappingRepository repository;

  public static final String EXCEPTION_TEMPLATE_MAPPING_EMPTY =
      "No content to map due to end-of-input";

  /** Tests store with template mapping as null */
  @Test
  public void testStoreNullTemplateMapping() {
    boolean exceptionThrown = false;
    try {
      templateMappingService.storeTemplateMappings("BSNOT", null);
    } catch (NullPointerException e) {
      exceptionThrown = true;
      assertEquals(null, e.getMessage());
    }

    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateMapping.class));
  }

  /** Tests store with template mapping empty */
  @Test
  public void testStoreEmptyTemplateMapping() {
    boolean exceptionThrown = false;

    ObjectMapper mapper = new ObjectMapper();
    List<TemplateMapping> myObjects = new ArrayList<>();

    try {
      myObjects =
          mapper.readValue(
              getClass().getResourceAsStream("/templates/freemarker/empty_template_mapping.json"),
              new TypeReference<List<TemplateMapping>>() {});
    } catch (IOException e) {
      exceptionThrown = true;
      assertThat(e.getMessage(), CoreMatchers.containsString(EXCEPTION_TEMPLATE_MAPPING_EMPTY));
    }

    try {
      templateMappingService.storeTemplateMappings("BSNOT", myObjects);
    } catch (NullPointerException e) {
      exceptionThrown = true;
    }
    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateMapping.class));
  }

  /**
   * Tests store with template mapping as valid
   *
   * @throws CTPException Exception thrown
   */
  @Test
  public void testStoreValidTemplateMapping() throws CTPException {

    ObjectMapper mapper = new ObjectMapper();
    List<TemplateMapping> myObjects = new ArrayList<>();

    try {
      myObjects =
          mapper.readValue(
              getClass().getResourceAsStream("/templates/freemarker/valid_template_mapping.json"),
              new TypeReference<List<TemplateMapping>>() {});
    } catch (IOException e) {
      System.out.println(e.getLocalizedMessage());
    }

    templateMappingService.storeTemplateMappings("BSNOT", myObjects);
    verify(repository, times(19)).save(any(TemplateMapping.class));
  }
}
