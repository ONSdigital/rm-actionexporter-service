[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5c09319b89ca4d0f8d9b88ed11c936e4)](https://www.codacy.com/app/sdcplatform/rm-actionexporter-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/rm-actionexporter-service&amp;utm_campaign=Badge_Grade) [![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/actionexportersvc.svg)]()
[![Build Status](https://travis-ci.org/ONSdigital/rm-actionexporter-service.svg?branch=master)](https://travis-ci.org/ONSdigital/rm-actionexporter-service)
[![codecov](https://codecov.io/gh/ONSdigital/rm-actionexporter-service/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/rm-actionexporter-service)

# Action Exporter Service
This repository contains the Action Exporter service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/).

## API
See [API.md](https://github.com/ONSdigital/rm-actionexporter-service/blob/master/API.md) for API documentation.

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
 