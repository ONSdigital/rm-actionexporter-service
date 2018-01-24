Sample Unit Ref:Enrolment Code:Survey Response Status:Account Status:First Name:Last Name:Email Address
<#list actionRequests as actionRequest>
${(actionRequest.address.sampleUnitRef?trim)!}:${actionRequest.iac?trim}:${(actionRequest.caseGroupStatus)!"null"}:${(actionRequest.enrolmentStatus)!"null"}:${(actionRequest.respondentStatus)!"null"}:${(actionRequest.contact.forename?trim)!"null"}:${(actionRequest.contact.surname?trim)!"null"}:${(actionRequest.contact.emailAddress)!"null"}:${(actionRequest.region)!"null"}
</#list>
${total}