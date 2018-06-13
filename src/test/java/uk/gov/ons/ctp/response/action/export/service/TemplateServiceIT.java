package uk.gov.ons.ctp.response.action.export.service;

import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageBase;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageSender;
import uk.gov.ons.ctp.response.action.export.config.AppConfig;
import uk.gov.ons.ctp.response.action.export.message.SftpServicePublisher;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionEvent;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.Priority;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static junit.framework.TestCase.assertEquals;


@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TemplateServiceIT {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private DefaultSftpSessionFactory defaultSftpSessionFactory;

    @Autowired
    private SftpServicePublisher sftpService;

    private SimpleMessageSender simpleMessageSender;
    private SimpleMessageListener simpleMessageListener;

    @Before
    public void setUp() {
        Rabbitmq rabbitConfig = this.appConfig.getRabbitmq();
        simpleMessageSender = new SimpleMessageSender(
                rabbitConfig.getHost(),
                rabbitConfig.getPort(),
                rabbitConfig.getUsername(),
                rabbitConfig.getPassword());

        simpleMessageListener = new SimpleMessageListener(
                rabbitConfig.getHost(),
                rabbitConfig.getPort(),
                rabbitConfig.getUsername(),
                rabbitConfig.getPassword());
    }

    @Test
    public void testTemplateGeneratesCorrectPrintFileForSocial() throws JAXBException, InterruptedException, IOException {
        // Given
        ActionRequest actionRequest = createSocialNotificationActionRequest();

        ActionInstruction actionInstruction = new ActionInstruction();
        actionInstruction.setActionRequest(actionRequest);

        JAXBContext jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
        StringWriter stringWriter = new StringWriter();
        jaxbContext.createMarshaller().marshal(actionInstruction, stringWriter);

        simpleMessageSender.sendMessage(
                "action-outbound-exchange",
                "Action.Printer.binding",
                stringWriter.toString());

        // When
        BlockingQueue<String> queue = simpleMessageListener.listen(
                SimpleMessageBase.ExchangeType.Fanout,
                "event-message-outbound-exchange");

        queue.take();

        // Then
        final String sftpPath = "Documents/sftp/";
        ChannelSftp.LsEntry[] sftpList = defaultSftpSessionFactory.getSession().list(sftpPath);
        Arrays.sort(sftpList, Comparator.comparingInt(o -> o.getAttrs().getMTime()));
        String notificationFilePath = sftpPath + sftpList[sftpList.length - 1].getFilename();
        InputStream inputSteam = defaultSftpSessionFactory.getSession().readRaw(notificationFilePath);

        CSVRecord templateRow = readFirstCsvRow(inputSteam);

        assertEquals(actionRequest.getAddress().getSampleUnitRef(), templateRow.get(0));
        assertEquals(actionRequest.getAddress().getLine1(), templateRow.get(1));
        assertEquals("", templateRow.get(2));
        assertEquals(actionRequest.getAddress().getPostcode(), templateRow.get(3));
        assertEquals(actionRequest.getAddress().getTownName(), templateRow.get(4));
        assertEquals(actionRequest.getIac(), templateRow.get(5));
        assertEquals(actionRequest.getCaseRef(), templateRow.get(6));

        defaultSftpSessionFactory.getSession().remove(notificationFilePath);

    }

    private CSVRecord readFirstCsvRow(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(':'));
        try {
            return parser.iterator().next();
        } finally {
            parser.close();
            reader.close();
        }
    }

    private ActionRequest createSocialNotificationActionRequest() {
        ActionAddress actionAddress = new ActionAddress();
        actionAddress.setSampleUnitRef("sampleUR");
        actionAddress.setLine1("Prem1");
        actionAddress.setPostcode("postCode");
        actionAddress.setTownName("postTown");

        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setActionId(UUID.randomUUID().toString());
        actionRequest.setActionPlan("actionPlan");
        actionRequest.setActionType("SOCIALNOT");
        actionRequest.setAddress(actionAddress);
        actionRequest.setQuestionSet("questions");
        actionRequest.setLegalBasis("legalBasis");
        actionRequest.setRegion("region");
        actionRequest.setRespondentStatus("rStatus");
        actionRequest.setEnrolmentStatus("eStatus");
        actionRequest.setCaseGroupStatus("cgStatus");
        actionRequest.setCaseId(UUID.randomUUID().toString());
        actionRequest.setPriority(Priority.HIGHEST);
        actionRequest.setCaseRef("caseRef");
        actionRequest.setIac("test-iac");
        actionRequest.setExerciseRef("exRef");
        actionRequest.setContact(new ActionContact());
        actionRequest.setEvents(new ActionEvent(Collections.singletonList("event1")));

        return actionRequest;
    }

}