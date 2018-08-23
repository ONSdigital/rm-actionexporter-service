UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.address.sampleUnitRef)!"null"}:' ||
'${(actionRequest.returnByDate)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialNotification';
