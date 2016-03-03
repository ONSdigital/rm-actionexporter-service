package uk.gov.ons.ctp.response.caseframe.endpoint;

import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.NON_EXISTING_SAMPLEID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.OUR_EXCEPTION_MESSAGE;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE1_CASETYPEID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE1_CRITERIA;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE1_DESC;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE1_NAME;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE2_CASETYPEID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE2_CRITERIA;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE2_DESC;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE2_NAME;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE3_CASETYPEID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE3_CRITERIA;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE3_DESC;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLE3_NAME;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SAMPLEID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.SURVEYID;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory.UNCHECKED_EXCEPTION;
import static uk.gov.ons.ctp.response.caseframe.utility.MockSurveyServiceFactory.NON_EXISTING_SURVEYID;

import javax.ws.rs.core.Application;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.jersey.CTPJerseyTest;
import uk.gov.ons.ctp.response.caseframe.CaseFrameBeanMapper;
import uk.gov.ons.ctp.response.caseframe.service.SampleService;
import uk.gov.ons.ctp.response.caseframe.utility.MockSampleServiceFactory;

/**
 * Created by Martin.Humphrey on 23/2/2016.
 */
public class SampleEndpointUnitTest extends CTPJerseyTest  {
  @Override
  public Application configure() {
    return super.init(SampleEndpoint.class, SampleService.class, MockSampleServiceFactory.class, new CaseFrameBeanMapper()); 
    
  }

  @Test
  public void findSamplesFound() {
    
    with("http://localhost:9998/samples")
    .assertResponseCodeIs(HttpStatus.OK)
    .assertArrayLengthInBodyIs(3)
    .assertStringListInBody("$..sampleName", SAMPLE1_NAME, SAMPLE2_NAME, SAMPLE3_NAME)
    .assertStringListInBody("$..description", SAMPLE1_DESC, SAMPLE2_DESC, SAMPLE3_DESC)
    .assertStringListInBody("$..addressCriteria", SAMPLE1_CRITERIA, SAMPLE2_CRITERIA, SAMPLE3_CRITERIA)
    .assertIntegerListInBody("$..caseTypeId", SAMPLE1_CASETYPEID, SAMPLE2_CASETYPEID,SAMPLE3_CASETYPEID)
    .assertIntegerListInBody("$..surveyId", SURVEYID,  SURVEYID, SURVEYID)
    .andClose();

  }
  
  @Test
  public void findSampleBySampleIdFound() {
    
    with("http://localhost:9998/samples/%s", SAMPLEID)
    .assertResponseCodeIs(HttpStatus.OK)
    .assertArrayLengthInBodyIs(6)
    .assertStringListInBody("$..sampleName", SAMPLE3_NAME)
    .assertStringListInBody("$..description", SAMPLE3_DESC)
    .assertStringListInBody("$..addressCriteria", SAMPLE3_CRITERIA)
    .assertIntegerListInBody("$..caseTypeId", SAMPLE3_CASETYPEID)
    .assertIntegerListInBody("$..surveyId", SURVEYID)    
    .andClose();

  }
  
  
  @Test
  public void findSampleBySampleIdNotFound() {
    
    with("http://localhost:9998/samples/%s", NON_EXISTING_SAMPLEID)
    .assertResponseCodeIs(HttpStatus.NOT_FOUND)
    .assertFaultIs(CTPException.Fault.RESOURCE_NOT_FOUND)
    .assertTimestampExists()
    .assertMessageEquals("Sample not found for id %s", NON_EXISTING_SURVEYID)
    .andClose();
    
  }

  @Test
  public void findSampleBysampleIdUnCheckedException() {

    with("http://localhost:9998/samples/%s", UNCHECKED_EXCEPTION)
    .assertResponseCodeIs(HttpStatus.INTERNAL_SERVER_ERROR)
    .assertFaultIs(CTPException.Fault.SYSTEM_ERROR)
    .assertTimestampExists()
    .assertMessageEquals(OUR_EXCEPTION_MESSAGE)
    .andClose();
    
  }
  
  @Test
  public void createCases() {    
    
    String putBody = "{\"geographyType\":\"LA\",\"geographyCode\":\"E07000163\"}";
    
    with("http://localhost:9998/samples/%s", SAMPLEID)
    .put(putBody)
    .assertResponseCodeIs(HttpStatus.NO_CONTENT)
    .assertEmptyResponse()
    .andClose();
  }  
  
}