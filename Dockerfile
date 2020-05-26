FROM openjdk:8-jre-slim

RUN apt-get update
COPY target/actionexportersvc-UNVERSIONED.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

