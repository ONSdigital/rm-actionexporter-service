[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5c09319b89ca4d0f8d9b88ed11c936e4)](https://www.codacy.com/app/sdcplatform/rm-actionexporter-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/rm-actionexporter-service&amp;utm_campaign=Badge_Grade) [![Docker Pulls](https://img.shields.io/docker/pulls/sdcplatform/actionexportersvc.svg)]()
[![Build Status](https://travis-ci.org/ONSdigital/rm-actionexporter-service.svg?branch=master)](https://travis-ci.org/ONSdigital/rm-actionexporter-service)

# Action Exporter Service
This repository contains the Action Exporter service. This microservice is a RESTful web service implemented using [Spring Boot](http://projects.spring.io/spring-boot/).

## API
See [API.md](https://github.com/ONSdigital/rm-actionexporter-service/blob/master/API.md) for API documentation.

## Copyright
Copyright (C) 2017 Crown Copyright (Office for National Statistics)
## To build
./mvnw clean install


## To run
    - Prerequisites:
        - Start MongoDB:
            - sudo mongod --dbpath /var/lib/mongodb
        - Start ActiveMQ:
            - sudo /sbin/service rabbitmq-server stop
            - cd /opt/apache-activemq-5.13.3/bin
            - ./activemq console

    - To start with default credentials:
        ./mvnw spring-boot:run

    - To start with specific credentials:
        ./mvnw spring-boot:run -Dsecurity.user.name=tiptop -Dsecurity.user.password=override


## To test
See curlTests.tx under /test/resources
