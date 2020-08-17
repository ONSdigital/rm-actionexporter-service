package uk.gov.ons.ctp.response.action.export.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.ons.ctp.response.action.export.utility.ObjectBuilder.buildListOfActionRequests;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.Address;
import uk.gov.ons.ctp.response.action.export.domain.Contact;

/** To unit test TemplateServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceImplTest {
  private static final String TEMPLATE_NAME = "testTemplate";
  private static final String TEST_FILE_PATH = "/tmp/ctp/forPrinter.csv";

  @InjectMocks private TemplateService templateService;

  @Mock private freemarker.template.Configuration configuration;

  @Test
  public void testTemplateGeneratesCorrectPrintFile() throws IOException, CTPException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
    cfg.setClassLoaderForTemplateLoading(
        Thread.currentThread().getContextClassLoader(), "templates/freemarker");
    cfg.setDefaultEncoding("UTF-8");
    Template template = cfg.getTemplate("initialPrint.ftl");
    Mockito.when(configuration.getTemplate("initialPrint")).thenReturn(template);
    ByteArrayOutputStream os = templateService.stream(testBusinessActionRequest(), "initialPrint");
    assertEquals(
        "SampleUnitRef:testIac:InProgress:Pending:Created:Richard:Weeks:richard.weeks@ons.gov.uk:null\n",
        os.toString());
  }

  private static List<ActionRequestInstruction> testBusinessActionRequest() {
    ActionRequestInstruction result = new ActionRequestInstruction();
    result.setSampleUnitRef("SampleUnitRef");
    Contact contact = new Contact();
    Address address = new Address();
    result.setActionId(UUID.randomUUID());
    result.setIac("testIac");
    result.setCaseGroupStatus("InProgress");
    result.setEnrolmentStatus("Pending");
    result.setRespondentStatus("Created");
    contact.setForename("Richard");
    contact.setSurname("Weeks");
    contact.setEmailAddress("richard.weeks@ons.gov.uk");
    result.setContact(contact);
    result.setAddress(address);
    return Collections.singletonList(result);
  }

  /** Tests stream issue retrieving */
  @Test
  public void testStreamIssueRetrievingTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenThrow(new IOException());
    boolean exceptionThrown = false;
    try {
      templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    } catch (RuntimeException e) {
      exceptionThrown = true;
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /** Tests stream null retrieving */
  @Test
  public void testStreamNullRetrievedTemplate() throws IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(null);
    boolean exceptionThrown = false;
    try {
      templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    } catch (RuntimeException e) {
      exceptionThrown = true;
    }
    TestCase.assertTrue(exceptionThrown);
  }

  /** Tests stream */
  @Test
  public void testStream() throws CTPException, IOException {
    Mockito.when(configuration.getTemplate(TEMPLATE_NAME)).thenReturn(Mockito.mock(Template.class));
    ByteArrayOutputStream result =
        templateService.stream(buildListOfActionRequests(), TEMPLATE_NAME);
    assertNotNull(result);
  }
}
