FROM openjdk:8-jre

VOLUME /tmp
COPY target/actionexportersvc-*.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "java",  "-jar", "/opt/actionexportersvc.jar" ]

