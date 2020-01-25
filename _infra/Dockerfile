FROM openjdk:8-jre-slim

RUN groupadd --gid 999 actionexporter && \
    useradd --create-home --system --uid 999 --gid actionexporter actionexporter
USER actionexporter

COPY actionexportersvc.jar /opt/actionexportersvc.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/actionexportersvc.jar" ]

