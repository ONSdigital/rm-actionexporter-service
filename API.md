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