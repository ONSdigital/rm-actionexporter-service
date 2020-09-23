package uk.gov.ons.ctp.response.action.export.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.printfile.Contact;
import uk.gov.ons.ctp.response.action.export.printfile.PrintFileEntry;

@Log4j
@Service
public class PrintFileService {

  @Autowired private Publisher publisher;

  public void send(String filename, List<ActionRequestInstruction> actionRequestInstructions) {
    List<PrintFileEntry> printFile = convertToPrintFile(actionRequestInstructions);
    try {
      String json = createJsonRepresentation(printFile);
      log.info("json");

      ByteString data = ByteString.copyFromUtf8(json);
      PubsubMessage pubsubMessage =
          PubsubMessage.newBuilder().setData(data).setMessageId(filename).build();

      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      String messageId = messageIdFuture.get();
      log.debug("messageId: " + messageId);
      log.debug("print file pubsub sent sucessfully");
      log.info(json);
    } catch (JsonProcessingException e) {
      log.error("unable to convert to json", e);
    } catch (InterruptedException | ExecutionException e) {
      log.error("pub/sub error", e);
    }
  }

  private String createJsonRepresentation(List<PrintFileEntry> printFile)
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(printFile);
  }

  protected List<PrintFileEntry> convertToPrintFile(
      List<ActionRequestInstruction> actionRequestInstructions) {
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
    return printFileEntries;
  }
}
