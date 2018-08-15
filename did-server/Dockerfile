FROM 10.19.248.200:30100/g_laikang/jre8-alpine:latest
MAINTAINER lixiangrong <lixiangrong@ennew.cn>

WORKDIR /opt

ARG SERVER_NAME
ENV SERVER_NAME=${SERVER_NAME}

COPY start.sh /opt/start.sh
COPY ${SERVER_NAME}*.jar /opt/

RUN chmod -R 755 /opt/


EXPOSE 16830 16831
ENTRYPOINT ["/opt/start.sh"]