UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.surveyAbbreviation + actionRequest.address.sampleUnitRef)!"null"}:' ||
'${(actionRequest.address.country)!"null"}
</#list>', datemodified = now()
where templatenamepk = 'socialPreNotification';