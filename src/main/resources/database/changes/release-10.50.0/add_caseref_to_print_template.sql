UPDATE actionexporter.template
SET content = '<#list actionRequests as actionRequest>
${(actionRequest.address.sampleUnitRef?trim)!}:${(actionRequest.iac?trim)!"null"}:${(actionRequest.caseGroupStatus)!"null"}:${(actionRequest.enrolmentStatus)!"null"}:${(actionRequest.respondentStatus)!"null"}:${(actionRequest.contact.forename?trim)!"null"}:${(actionRequest.contact.surname?trim)!"null"}:${(actionRequest.contact.emailAddress)!"null"}:${(actionRequest.region)!"null"}:${(actionRequest.caseRef)!"null"}
</#list>'
WHERE templatenamepk ='initialPrint'