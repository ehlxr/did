FROM anapsix/alpine-java:latest
MAINTAINER ehlxr <ehlxr.me@gmail.com>

WORKDIR /opt

ARG SERVER_NAME
ENV SERVER_NAME=${SERVER_NAME}

COPY start.sh /opt/start.sh
COPY ${SERVER_NAME}*.jar /opt/

RUN chmod -R 755 /opt/


EXPOSE 16830 16831
ENTRYPOINT ["/opt/start.sh"]