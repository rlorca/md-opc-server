FROM openjdk:8-jre

# create app user
RUN useradd -ms /bin/bash opc-server

WORKDIR /home/opc-server

USER opc-server

ARG JAR_FILE
ADD target/${JAR_FILE} /home/opc-server/server.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/home/opc-server/server.jar"]