UPDATE actionexporter.template
SET content = '<#list actionRequests as actionRequest>
${(actionRequest.address.sampleUnitRef?trim)!}:${actionRequest.iac?trim}:${(actionRequest.contact.forename?trim)!"null"}:${(actionRequest.contact.emailAddress)!"null"}
  </#list>';
