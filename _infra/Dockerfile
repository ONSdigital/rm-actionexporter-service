FROM openjdk:8-jre-slim

COPY actionexportersvc.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

