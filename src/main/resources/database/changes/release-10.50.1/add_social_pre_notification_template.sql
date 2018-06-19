INSERT INTO actionexporter.template
(templatenamepk, content, datemodified)
VALUES('socialPreNotification', '<#list actionRequests as actionRequest>
${(actionRequest.address.line1?trim)!}:' ||
'${(actionRequest.address.line2?trim)!}:' ||
'${(actionRequest.address.postcode?trim)!}:' ||
'${(actionRequest.address.townName?trim)!}:' ||
'${(actionRequest.address.locality?trim)!}:' ||
'${(actionRequest.caseRef)!"null"}
</#list>', '2018-06-12 11:29:45.226');
