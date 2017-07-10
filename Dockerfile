FROM openjdk:8u121-jre 
MAINTAINER Kieran Wardle <kieran.wardle@ons.gov.uk>
ARG jar
VOLUME /tmp
COPY $jar actionexportersvc.jar
RUN sh -c 'touch /actionexportersvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /actionexportersvc.jar" ]

