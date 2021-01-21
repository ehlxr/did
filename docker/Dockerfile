FROM openjdk:8-jre-alpine
MAINTAINER ehlxr <ehlxr.me@gmail.com>

WORKDIR /opt

ARG SERVER_NAME

ENV SERVER_NAME=${SERVER_NAME}
ENV SDK_PORT=16831
ENV HTTP_PORT=16830

COPY ./docker/ /opt/

RUN chmod -R 755 /opt/

EXPOSE ${SDK_PORT} ${HTTP_PORT}
ENTRYPOINT ["/opt/start.sh"]