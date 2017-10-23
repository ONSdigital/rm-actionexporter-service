package uk.gov.ons.ctp.response.action.export.service;

import freemarker.template.Template;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateExpression;
import uk.gov.ons.ctp.response.action.export.repository.TemplateRepository;
import uk.gov.ons.ctp.response.action.export.service.impl.TemplateServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.action.export.service.impl.TemplateServiceImpl.ERROR_RETRIEVING_FREEMARKER_TEMPLATE;
import static uk.gov.ons.ctp.response.action.export.service.impl.TemplateServiceImpl.EXCEPTION_STORE_TEMPLATE;
import static uk.gov.ons.ctp.response.action.export.utility.ObjectBuilder.buildListOfActionRequests;

/**
 * To unit test TemplateServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceImplTest {
  private static final String TEMPLATE_NAME = "testTemplate";
  private static final String TEST_FILE_PATH = "/tmp/ctp/forPrinter.csv";

  @InjectMocks
  private TemplateServiceImpl templateService;

  @Mock
  private TemplateRepository repository;

  @Mock
  private freemarker.template.Configuration configuration;

  /**
   * Tests store with a null template
   */
  @Test
  public void testStoreNullTemplate() {
    boolean exceptionThrown = false;
    try {
      templateService.storeTemplate(TEMPLATE_NAME, null);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(EXCEPTION_STORE_TEMPLATE, e.getMessage());
    }
    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateExpression.class));
    verify(configuration, times(0)).clearTemplateCache();
  }

  /**
   * Tests store with an empty template
   */
  @Test
  public void testStoreEmptyTemplate() {
    boolean exceptionThrown = false;
    try {
      templateService.storeTemplate(TEMPLATE_NAME, getClass().getResourceAsStream(
              "/templates/freemarker/empty_template.ftl"));
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(EXCEPTION_STORE_TEMPLATE, e.getMessage());
    }
    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateExpression.class));
    verify(configuration, times(0)).clearTemplateCache();
  }


  /**
   * Tests store with an valid template
   */
  @Test
  public void testStoreValidTemplate() throws CTPException {
    templateService.storeTemplate(TEMPLATE_NAME, getClass().getResourceAsStream(
            "/templates/freemarker/valid_template.ftl"));
    verify(repository, times(1)).save(any(TemplateExpression.class));
    verify(configuration, times(1)).clearTemplateCache();
  }


  /**
   * Tests file issue retrieving template
   */
  @Test
  public void testFileIssueRetrievingTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenThrow(new IOException());
    boolean exceptionThrown = false;
    try {
      templateService.file(buildListOfActionRequests(), TEMPLATE_NAME, TEST_FILE_PATH);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /**
   * Tests file null retrieving template
   */
  @Test
  public void testFileNullRetrievedTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(null);
    boolean exceptionThrown = false;
    try {
      templateService.file(buildListOfActionRequests(), TEMPLATE_NAME, TEST_FILE_PATH);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(ERROR_RETRIEVING_FREEMARKER_TEMPLATE, e.getMessage());
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /**
   * Tests file
   */
//  @Test
//  public void testFile() throws CTPException, IOException {
//    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(Mockito.mock(Template.class));
//
//    File result = templateService.file(buildListOfActionRequests(), TEMPLATE_NAME, TEST_FILE_PATH);
//    assertNotNull(result);
//  }

  /**
   * Tests stream issue retrieving 
   */
  @Test
  public void testStreamIssueRetrievingTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenThrow(new IOException());
    boolean exceptionThrown = false;
    try {
      templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /**
   * Tests stream null retrieving 
   */
  @Test
  public void testStreamNullRetrievedTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(null);
    boolean exceptionThrown = false;
    try {
      templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(ERROR_RETRIEVING_FREEMARKER_TEMPLATE, e.getMessage());
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /**
   * Tests stream
   */
  @Test
  public void testStream() throws CTPException, IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(Mockito.mock(Template.class));
    ByteArrayOutputStream result = templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    assertNotNull(result);
  }
}
