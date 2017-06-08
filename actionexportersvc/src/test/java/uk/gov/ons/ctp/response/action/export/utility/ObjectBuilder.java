package uk.gov.ons.ctp.response.action.export.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.domain.Address;

/**
 * Utility class to build objects required in tests
 */
public class ObjectBuilder {
  public static List<ActionRequestInstruction> buildListOfActionRequests() {
    List<ActionRequestInstruction> result = new ArrayList<>();
    for (int i = 1; i < 51; i++) {
      result.add(buildActionRequest(i));
    }
    return result;
  }

  private static ActionRequestInstruction buildActionRequest(int i) {
    ActionRequestInstruction result =  new ActionRequestInstruction();
    result.setActionId(UUID.randomUUID());
    result.setActionType("testActionType");
    result.setIac("testIac");
    result.setAddress(buildActionAddress());
    return result;
  }

  private static Address buildActionAddress() {
    Address address = new Address();
    address.setLine1("1 High Street");
    address.setTownName("Southampton");
    address.setPostcode("SO16 0AS");
    return address;
  }

}
