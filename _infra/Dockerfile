FROM openjdk:8-jre-slim

VOLUME /tmp
RUN apt-get update
RUN apt-get -yq install curl
RUN apt-get -yq clean
COPY actionexportersvc.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]
