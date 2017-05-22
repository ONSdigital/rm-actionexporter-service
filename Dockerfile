FROM openjdk 
ARG jar
VOLUME /tmp
ADD $jar actionexportersvc.jar
RUN sh -c 'touch /actionexportersvc.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java -jar /actionexportersvc.jar" ]

