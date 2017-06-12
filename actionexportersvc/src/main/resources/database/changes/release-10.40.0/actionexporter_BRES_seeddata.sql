
set schema 'actionexporter';

INSERT INTO template (templatenamePK,content,datemodified)
VALUES ('initialPrint','<#list actionRequests as actionRequest>
${actionRequest.iac?trim}|${(actionRequest.caseRef)!}|${(actionRequest.contact.title)!}|${(actionRequest.contact.forename)!}|${(actionRequest.contact.surname)!}|${(actionRequest.address.organisationName)!}|${(actionRequest.address.line1)!}|${(actionRequest.address.line2)!}|${(actionRequest.address.locality)!}|${(actionRequest.address.townName)!}|${(actionRequest.address.postcode)!}
</#list>'  , now());

INSERT INTO templatemapping (actiontypenamePK,templatenameFK,file,datemodified) VALUES ('BRESEL' , 'initialPrint', 'BRESEL', now());
INSERT INTO templatemapping (actiontypenamePK,templatenameFK,file,datemodified) VALUES ('BRESERL', 'initialPrint', 'BRESERL', now());


