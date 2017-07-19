UPDATE actionexporter.template
SET content = '<#list actionRequests as actionRequest>
  ${(actionRequest.address.sampleUnitRef)!}:${actionRequest.iac?trim}:${(actionRequest.contact.forename?trim)!}:${(actionRequest.contact.emailaddress)!}
  </#list>';