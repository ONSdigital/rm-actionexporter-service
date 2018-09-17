INSERT INTO actionexporter.template (templatenamepk, content, datemodified) VALUES ('socialReminder',
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.address.country?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.address.organisationName?trim)!}:' ||
'${(actionRequest.address.sampleUnitRef)!"null"}:' ||
'${(actionRequest.returnByDate)!"null"}
</#list>'
,
now());

UPDATE actionexporter.templatemapping SET templatenamefk = 'socialReminder' WHERE actiontypenamepk = 'SOCIALREM';
