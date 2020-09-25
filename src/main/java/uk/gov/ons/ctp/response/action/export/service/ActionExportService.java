package uk.gov.ons.ctp.response.action.export.service;

import java.sql.Timestamp;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.AddressRepository;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;

/** Service implementation responsible for persisting action export requests */
@Service
public class ActionExportService {
  private static final Logger log = LoggerFactory.getLogger(ActionExportService.class);

  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

  private static final int TRANSACTION_TIMEOUT = 60;

  @Qualifier("actionExporterBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  @Autowired private ActionRequestRepository actionRequestRepo;

  @Autowired private AddressRepository addressRepo;

  @Transactional(
      propagation = Propagation.REQUIRED,
      readOnly = false,
      timeout = TRANSACTION_TIMEOUT)
  public void acceptInstruction(ActionInstruction instruction) {
    if (instruction.getActionRequest() != null) {
      processActionRequest(instruction.getActionRequest());
    } else {
      log.info("No ActionRequest to process");
      if (instruction.getActionCancel() != null) {
        log.error("cancel actions no longer supported");
      } else {
        log.info("No ActionCancel to process");
      }
    }
  }

  /**
   * To process an ActionRequest
   *
   * @param actionRequest to be processed
   */
  private void processActionRequest(ActionRequest actionRequest) {
    log.debug(
        "action_id: "
            + actionRequest.getActionId()
            + ", case_id: "
            + actionRequest.getCaseId()
            + ", action_type: "
            + actionRequest.getActionType()
            + ", Saving actionRequest");

    ActionRequestInstruction actionRequestDoc =
        mapperFacade.map(actionRequest, ActionRequestInstruction.class);

    Timestamp now = DateTimeUtil.nowUTC();
    actionRequestDoc.setDateStored(now);

    if (actionRequestDoc.getAddress() != null) {
      actionRequestDoc.getAddress().setAddressPK(UUID.randomUUID());
      addressRepo.persist(actionRequestDoc.getAddress());
    }

    if (actionRequestRepo.existsByActionId(actionRequestDoc.getActionId())) {
      // ActionRequests should never be sent twice with same actionId but...
      log.warn("action_id: ", actionRequestDoc.getActionId() + ", key ActionId already exists");
    } else {
      actionRequestRepo.persist(actionRequestDoc);
    }
  }
}
