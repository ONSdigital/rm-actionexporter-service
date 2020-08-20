[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5c09319b89ca4d0f8d9b88ed11c936e4)](https://www.codacy.com/app/sdcplatform/rm-actionexporter-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/rm-actionexporter-service&amp;utm_campaign=Badge_Grade) [![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/actionexportersvc.svg)]()
[![Build Status](https://travis-ci.org/ONSdigital/rm-actionexporter-service.svg?branch=master)](https://travis-ci.org/ONSdigital/rm-actionexporter-service)
[![codecov](https://codecov.io/gh/ONSdigital/rm-actionexporter-service/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/rm-actionexporter-service)

# Action Exporter Service
This repository contains the Action Exporter service and is microservice implemented using [Spring Boot](http://projects.spring.io/spring-boot/).

The Action Exporter uses data from the Action service to generate a file. Once created this file is SFTP to MoveIt for 
use within the ONS. This is generally a print file which is then used to produce physical letters.

This service receives instructions from the [Action Service](https://github.com/ONSdigital/rm-action-service) on the 
Action.Printer queue in the Rabbit MQ cluster. These are action request objects and contain XML an example of which is shown below:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:actionInstruction xmlns:ns2="http://ons.gov.uk/ctp/response/action/message/instruction">
    <actionRequest>
    <actionId>4ccdd82b-02ae-4e0f-b432-669bdb34ddac</actionId>
    <responseRequired>false</responseRequired>
    <actionPlan>dd38d193-16ad-44a3-bacb-74ff99c48117</actionPlan>
    <actionType>BSNL</actionType><contact>
    <ruName>Example LTD</ruName>
    <tradingStyle>  
    </tradingStyle>
    </contact>
    <legalBasis>Statistics of Trade Act 1947</legalBasis>
    <region>FE</region>
    <caseGroupStatus>NOTSTARTED</caseGroupStatus>
    <caseId>d3e6c988-5dad-488c-b39f-fdc8b8f98270</caseId>
    <priority>medium</priority><caseRef>1000000000000016</caseRef>
    <iac>gq6rgf5wddio</iac>
    <events>
        <event>CASE_CREATED : null : SYSTEM : Case created when Initial creation of case</event>
    </events>
    <exerciseRef>202005</exerciseRef>
    <userDescription>May 2020</userDescription>
    <surveyName>Quarterly Business Survey</surveyName>
    <surveyRef>139</surveyRef>
    <returnByDate>11/06/2020</returnByDate>
    <sampleUnitRef>1000000001</sampleUnitRef>
    </actionRequest>
</ns2:actionInstruction>
```

These instructions are immediately stored in the database in the action request table when they are received by this service
at that point they do not have an export job id associated with them. This is how this service determines which entries
need to be exported.

A scheduled cron job, the ExportScheduler (which runs every minute locally but hourly in production) is responsible 
for taking these entries applying a template to the data set and generating a file. 

This is done by the ExportProcessor by querying the action request table for any entries which do not have an export id. 
At that point a new export job is created and the entries in the action request table without an export id are updated 
with the id of the new job.
 
The export job then executes, it queries the database it has just updated for entries with that specific job id, once it 
has them it runs the freemarker template engine against each entry and constructs a file in memory. The Export Processor 
finishes once all entries have been processed and a file exists in memory.

At that point the file is handed over to the Notification File Creator. That creates an entry in the Export File table 
with the id of the job that processed it. It then calls the SFTP publisher service and also publishes an event.

The SFTP publisher service is based on Spring integration and the flow can be found in sftp-outbound-flow.xml. If the flow is 
successful the Export File table is updated with a date time of when it was sent and a success statues. However if it was 
unsuccesful the Export File is updated with only a failed status. 

An entry is also created in the Export Report Repository (aka filerowcount table) with the number of rows in the file, 
a flag to determine if it was sent successful and another flag to indicate if it has been reported (which for some reason
 is always false). 
 
If the transfer is unsuccessful unfortunately at that point the generated file is lost and cannot be recreated. (Note: 
we implement a temporary push to a GCP bucket so that if the SFTP process fails we can recover the file).

There is a distributed lock on REDIS to ensure only one action exporter can run this process at a time. It also uses a 
single consuming thread on it's rabbit connection to ensure messages are processed sequentially.

The action exporter can support multiple templates and these are stored in the templates table. In the action request above 
there is an action type and there is a mapping between action type and template in the template mappings table. There is 
a Rest endpoint for updating and querying this table however the main have templates have been populated by liquibase during
the setup of the database. However in production we currently only use a single template.

## API
See the [API](API.yaml) for API documentation.

## Database structure.
See the [DATABASE](DATABASE.md) for details

## Suggestions
Suggestions for improvements can be found in the [improvements](IMPROVEMENTS.md)

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)

## Running

There are two ways of running this service

* The easiest way is via docker (https://github.com/ONSdigital/ras-rm-docker-dev)
* Alternatively running the service up in isolation
    ```bash
    cp .maven.settings.xml ~/.m2/settings.xml  # This only needs to be done once to set up mavens settings file
    mvn clean install
    mvn spring-boot:run
    ```
# Code Style
`mvn install` will automatically format the code using googles style. IDE plugins can be found here [Google java format](https://github.com/google/google-java-format#intellij)
 
