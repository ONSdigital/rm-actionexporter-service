ARG JAR_FILE=actionexportersvc-*.jar
FROM openjdk:8-jre

VOLUME /tmp
ARG JAR_FILE
COPY target/$JAR_FILE /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

