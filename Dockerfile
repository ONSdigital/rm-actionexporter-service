FROM openjdk:8-jre-slim

VOLUME /tmp
ARG JAR_FILE=actionexportersvc-*.jar
RUN apt-get update
RUN apt-get -yq install curl
RUN apt-get -yq clean
COPY target/$JAR_FILE /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

