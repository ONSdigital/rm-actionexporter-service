FROM openjdk:11-jre-slim

RUN apt-get update
COPY target/actionexporter.jar /opt/actionexporter.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexporter.jar" ]

