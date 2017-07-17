# Action Exporter Service API
This page documents the Action Exporter service API endpoints. All endpoints return an `HTTP 200 OK` status code except where noted otherwise.

## Service Information
* `GET /info` will return information about this service, collated from when it was last built.

### Example JSON Response
```json
{
    "name": "actionexportersvc",
    "version": "10.42.0",
    "origin": "git@github.com:ONSdigital/rm-actionexporter-service.git",
    "commit": "a59450c6b028824bc59ef33be6d697f1a75262ac",
    "branch": "master",
    "built": "2017-07-12T15:20:07Z"
}
```

## List Templates
* `GET /templates` will return a list of all Action Exporter templates.

### Example JSON Response
```json
[
    {
        "name": "initialPrint",
        "content": "<#list actionRequests as actionRequest>\n  ${(actionRequest.address.sampleUnitRef)!}|${actionRequest.iac?trim}|${(actionRequest.contact.forename?trim)!}|${(actionRequest.contact.emailaddress)!}\n  </#list>",
        "dateModified": "2017-07-14T12:00:00Z"
    }
]
```

An `HTTP 204 No Content` status code is returned if there are no Action Exporter templates.

## Get Template
* `GET /templates/initialPrint` will return the details of the Action Exporter template with the name `initialPrint`.

### Example JSON Response
```json
{
    "name": "initialPrint",
    "content": "<#list actionRequests as actionRequest>\n  ${(actionRequest.address.sampleUnitRef)!}|${actionRequest.iac?trim}|${(actionRequest.contact.forename?trim)!}|${(actionRequest.contact.emailaddress)!}\n  </#list>",
    "dateModified": "2017-07-14T12:00:00Z"
}
```

An `HTTP 404 Not Found` status code is returned if the Action Exporter template with the specified name could not be found.

## Create Template
* `POST /templates/initialPrint` will upload a [FreeMarker](http://freemarker.org/) template to the Action Exporter template with the name `initialPrint`.

**Required parameters**: `file` as the FreeMarker template form-data.

### Example JSON Response
```json
{
    "name": "initialPrint",
    "content": "<#list actionRequests as actionRequest>\n  ${(actionRequest.address.sampleUnitRef)!}|${actionRequest.iac?trim}|${(actionRequest.contact.forename?trim)!}|${(actionRequest.contact.emailaddress)!}\n  </#list>",
    "dateModified": "2017-07-14T12:00:00Z"
}
```

An `HTTP 201 Created` status code is returned if the FreeMarker template update was a success. An `HTTP 400 Bad Request` is returned if the input data was invalid.

## List Template Mappings
* `GET /templatemappings` will return a list of all mappings for Action Exporter templates.

### Example JSON Response
```json
[
    {
        "actionType": "BSNOT",
        "template": "initialPrint",
        "file": "BSNOT",
        "dateModified": "2017-07-14T12:00:00Z"
    },
    {
        "actionType": "BSREM",
        "template": "initialPrint",
        "file": "BSREM",
        "dateModified": "2017-07-14T12:00:00Z"
    }
]
```

An `HTTP 204 No Content` status code is returned if there are no Action Exporter templates.