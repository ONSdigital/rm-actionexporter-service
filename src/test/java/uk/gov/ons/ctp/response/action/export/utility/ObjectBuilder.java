package uk.gov.ons.ctp.response.action.export.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.Address;
import uk.gov.ons.ctp.response.action.export.domain.Contact;

/** Utility class to build objects required in tests */
public class ObjectBuilder {

  /**
   * Builds a list of Action Requests
   *
   * @return list of ActionRequests
   */
  public static List<ActionRequestInstruction> buildListOfActionRequests() {
    List<ActionRequestInstruction> result = new ArrayList<>();
    for (int i = 1; i < 51; i++) {
      result.add(buildActionRequest());
    }
    return result;
  }

  /**
   * Builds an Action Request
   *
   * @return ActionRequest object
   */
  private static ActionRequestInstruction buildActionRequest() {
    ActionRequestInstruction result = new ActionRequestInstruction();
    result.setActionId(UUID.randomUUID());
    result.setActionType("testActionType");
    result.setIac("testIac");
    result.setAddress(buildActionAddress());
    result.setSurveyRef("testSurveyRef");
    result.setCaseGroupStatus("testCaseGroupStatus");
    result.setEnrolmentStatus("testEnrolmentStatus");
    result.setRespondentStatus("testRespondentStatus");
    result.setContact(buildContact());
    result.setRegion("testRegion");
    result.setSampleUnitRef("testSampleUnitRef");
    return result;
  }

  private static Contact buildContact() {
    Contact contact = new Contact();
    contact.setEmailAddress("testEmailAddress");
    contact.setForename("testForename");
    contact.setSurname("testSurname");
    return contact;
  }

  /**
   * Builds an Action Address
   *
   * @return ActionAddress object
   */
  private static Address buildActionAddress() {
    Address address = new Address();
    address.setAddressPK(UUID.randomUUID());
    address.setLine1("1 High Street");
    address.setTownName("Southampton");
    address.setPostcode("SO16 0AS");
    return address;
  }
}
