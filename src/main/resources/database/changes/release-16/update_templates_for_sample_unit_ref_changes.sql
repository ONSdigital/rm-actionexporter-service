UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.address.country?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.address.organisationName?trim)!}:' ||
'${(actionRequest.sampleUnitRef)!"null"}:' ||
'${(actionRequest.returnByDate)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialReminder';

UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.address.country?trim)!}:' ||
'${(actionRequest.sampleUnitRef)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialPreNotification';

UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.address.country?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.sampleUnitRef)!"null"}:' ||
'${(actionRequest.returnByDate)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialNotification';

UPDATE actionexporter.template
SET content = '<#list actionRequests as actionRequest>
${(actionRequest.sampleUnitRef?trim)!}:${(actionRequest.iac?trim)!"null"}:${(actionRequest.caseGroupStatus)!"null"}:${(actionRequest.enrolmentStatus)!"null"}:${(actionRequest.respondentStatus)!"null"}:${(actionRequest.contact.forename?trim)!"null"}:${(actionRequest.contact.surname?trim)!"null"}:${(actionRequest.contact.emailAddress)!"null"}:${(actionRequest.region)!"null"}
</#list>'
WHERE templatenamepk ='initialPrint'