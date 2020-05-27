FROM openjdk:8-jre-slim

COPY target/actionexporter.jar /opt/actionexporter.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexporter.jar" ]
