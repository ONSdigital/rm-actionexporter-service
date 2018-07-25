UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.line3?trim)!}:' ||
'${(actionRequest.address.line4?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.iac?trim)!"null"}:' ||
'${(actionRequest.address.sampleUnitRef)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialNotification'