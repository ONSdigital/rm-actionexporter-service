FROM openjdk:8-jre-slim

VOLUME /tmp
ARG JAR_FILE=actionexportersvc*.jar
RUN apt-get update
RUN apt-get -yq clean

RUN groupadd -g 989 actionexportersvc && \
    useradd -r -u 989 -g actionexportersvc actionexportersvc
USER actionexportersvc

COPY target/$JAR_FILE /opt/actionexportersvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/actionexportersvc.jar" ]
