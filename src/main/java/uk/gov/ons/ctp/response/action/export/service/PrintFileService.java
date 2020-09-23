package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.printfile.Contact;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFile;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFileEntry;

@Log4j
@Service
public class PrintFileService {

  public void send(List<ActionRequestInstruction> actionRequestInstructions) {
    PrintFile printFile = convertToPrintFile(actionRequestInstructions);
    try {
      String json = createJsonRepresentation(printFile);
      log.info("json");
      log.info(json);
    } catch (JsonProcessingException e) {
      log.error("unable to convert to json");
    }
  }

  private String createJsonRepresentation(PrintFile printFile) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(printFile);
  }

  protected PrintFile convertToPrintFile(List<ActionRequestInstruction> actionRequestInstructions) {
    List<PrintFileEntry> printFileEntries = new ArrayList<>();

    for (ActionRequestInstruction actionRequestInstruction : actionRequestInstructions) {
      PrintFileEntry entry = new PrintFileEntry();

      entry.setCaseGroupStatus(actionRequestInstruction.getCaseGroupStatus());
      entry.setIac(actionRequestInstruction.getIac());
      entry.setEnrolmentStatus(actionRequestInstruction.getEnrolmentStatus());
      entry.setRegion(actionRequestInstruction.getRegion());
      entry.setRespondentStatus(actionRequestInstruction.getRespondentStatus());
      entry.setSampleUnitRef(actionRequestInstruction.getSampleUnitRef());
      if (actionRequestInstruction.getContact() != null) {
        Contact contact = new Contact();
        contact.setEmailAddress(actionRequestInstruction.getContact().getEmailAddress());
        contact.setForename(actionRequestInstruction.getContact().getForename());
        contact.setSurname(actionRequestInstruction.getContact().getSurname());
        entry.setContact(contact);
      }
      printFileEntries.add(entry);
    }
    PrintFile printFile = new PrintFile();
    printFile.setPrintFileEntries(printFileEntries);
    return printFile;
  }
}
