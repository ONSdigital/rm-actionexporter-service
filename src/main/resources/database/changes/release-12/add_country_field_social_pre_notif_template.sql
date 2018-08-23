UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.address.country?trim)!}:' ||
'${(actionRequest.address.sampleUnitRef)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialPreNotification';
