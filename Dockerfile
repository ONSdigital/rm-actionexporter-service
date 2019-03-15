FROM openjdk:8-jre-slim

VOLUME /tmp
ARG JAR_FILE=actionexportersvc*.jar
RUN apt-get update
RUN apt-get -yq clean

RUN groupadd --gid 989 actionexportersvc && \
    useradd --create-home --system --uid 989 --gid actionexportersvc actionexportersvc
USER actionexportersvc

COPY target/$JAR_FILE /opt/actionexportersvc.jar

ENTRYPOINT [ "java", "-jar", "/opt/actionexportersvc.jar" ]
