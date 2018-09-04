package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.common.time.DateTimeUtil;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;
import uk.gov.ons.ctp.response.action.export.message.ActionFeedbackPublisher;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.repository.AddressRepository;
import uk.gov.ons.ctp.response.action.message.feedback.ActionFeedback;
import uk.gov.ons.ctp.response.action.message.feedback.Outcome;
import uk.gov.ons.ctp.response.action.message.instruction.ActionCancel;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;

/** Service implementation responsible for persisting action export requests */
@Service
public class ActionExportService {
  private static final Logger log = LoggerFactory.getLogger(ActionExportService.class);

  private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

  private static final int TRANSACTION_TIMEOUT = 60;

  @Autowired private ActionFeedbackPublisher actionFeedbackPubl;

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
        processActionCancel(instruction.getActionCancel());
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
    log.with("action_request", actionRequest).debug("Saving actionRequest");
    ActionRequestInstruction actionRequestDoc =
        mapperFacade.map(actionRequest, ActionRequestInstruction.class);

    Timestamp now = DateTimeUtil.nowUTC();
    actionRequestDoc.setDateStored(now);

    if (!addressRepo.existsBySampleUnitRef(actionRequestDoc.getAddress().getSampleUnitRef())) {
      // Address should never change so do not save if already exists
      addressRepo.persist(actionRequestDoc.getAddress());
    }

    if (actionRequestRepo.existsByActionId(actionRequestDoc.getActionId())) {
      // ActionRequests should never be sent twice with same actionId but...
      log.with("action_id", actionRequestDoc.getActionId()).warn("Key ActionId already exists");
    } else {
      actionRequestRepo.persist(actionRequestDoc);
    }

    if (actionRequestDoc.isResponseRequired()) {
      String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(now);
      ActionFeedback actionFeedback =
          new ActionFeedback(
              actionRequestDoc.getActionId().toString(),
              "ActionExport Stored: " + timeStamp,
              Outcome.REQUEST_ACCEPTED);
      actionFeedbackPubl.sendActionFeedback(actionFeedback);
    }
  }

  /**
   * To process an ActionCancel
   *
   * @param actionCancel to be processed
   */
  private void processActionCancel(ActionCancel actionCancel) {
    log.with("action_cancel", actionCancel).debug("Processing actionCancel");
    ActionRequestInstruction actionRequest =
        actionRequestRepo.findOne(UUID.fromString(actionCancel.getActionId()));

    boolean cancelled = false;
    if (actionRequest != null && actionRequest.getDateSent() == null) {
      actionRequestRepo.delete(UUID.fromString(actionCancel.getActionId()));
      cancelled = true;
    } else {
      cancelled = false;
    }

    if (actionCancel.isResponseRequired()) {
      String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
      ActionFeedback actionFeedback =
          new ActionFeedback(
              actionCancel.getActionId(),
              "ActionExport Cancelled: " + timeStamp,
              cancelled ? Outcome.CANCELLATION_COMPLETED : Outcome.CANCELLATION_FAILED);
      actionFeedbackPubl.sendActionFeedback(actionFeedback);
    }
  }
}
