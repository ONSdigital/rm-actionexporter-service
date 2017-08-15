UPDATE actionexporter.template
SET content = '<#assign actionRequest\.contact\.forename = ""> <#assign actionRequest\.contact\.emailaddress = "">
<#list actionRequests as actionRequest>
${(actionRequest.address.sampleUnitRef?trim)!}:${actionRequest.iac?trim}:<#if actionRequest\.contact\.forename?? || actionRequest\.contact\.forename == NULL>null<#else> actionRequest.contact.forename?trim </#if>:<#if actionRequest\.contact\.emailaddress?? || actionRequest\.contact\.emailaddress == NULL>null<#else> actionRequest.contact.emailaddress?trim </#if>
  </#list>';
