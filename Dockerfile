FROM openjdk:8-jre

VOLUME /tmp
COPY target/actionexportersvc-*.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

