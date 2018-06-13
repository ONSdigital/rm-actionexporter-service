INSERT INTO actionexporter.template
(templatenamepk, content, datemodified)
VALUES('socialNotification', '<#list actionRequests as actionRequest>
${(actionRequest.address.sampleUnitRef?trim)!}:' ||
'${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.caseRef)!"null"}
</#list>', '2018-06-12 11:29:45.226');
