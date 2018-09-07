INSERT INTO actionexporter.template (templatenamepk, content, datemodified) VALUES ('socialRemNotification',
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

UPDATE templatemapping SET templatenamefk = 'socialRemNotification' WHERE actiontypenamepk = 'SOCIALREM';