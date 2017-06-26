
set schema 'actionexporter';

INSERT INTO template (templatenamePK,content,datemodified)
VALUES ('initialPrint','<#list actionRequests as actionRequest>
  ${(actionRequest.address.sampleunitrefpk)!}|${actionRequest.iac?trim}|${(actionRequest.contact.forename)!}|${(actionRequest.contact.surname)!}|${(actionRequest.contact.emailaddress)!}
  </#list>'  , now());

INSERT INTO templatemapping (actiontypenamePK,templatenameFK,file,datemodified) VALUES ('BRESEL' , 'initialPrint', 'BRESEL', now());
INSERT INTO templatemapping (actiontypenamePK,templatenameFK,file,datemodified) VALUES ('BRESERL', 'initialPrint', 'BRESERL', now());
