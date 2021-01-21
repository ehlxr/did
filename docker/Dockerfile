FROM openjdk:8-jre-alpine
MAINTAINER ehlxr <ehlxr.me@gmail.com>

WORKDIR /opt

ARG SERVER_NAME
ENV SERVER_NAME=${SERVER_NAME}

COPY ./docker/ /opt/

RUN chmod -R 755 /opt/

EXPOSE 16830 16831
ENTRYPOINT ["/opt/start.sh"]