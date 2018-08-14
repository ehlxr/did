#!/bin/sh

#HOSTNAME=`hostname | cut -c1-18`
LOGS_DIR="/data/logs"

if [ ! -d ${LOGS_DIR} ]; then
	mkdir -p ${LOGS_DIR}
fi
STDOUT_FILE=${LOGS_DIR}/${SERVER_NAME}.log

JAVA_OPTS=" -Duser.timezone=\"Asia/Shanghai\" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF8"

JAVA_MEM_OPTS=" -server -Xmx${XMX:-512m} -Xms${XMS:-128m} -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC \
    -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=${LargePageSize:-4m} \
    -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "

echo "Starting the ${SERVER_NAME} ...\c"
#java -jar ${JAVA_OPTS} ${JAVA_MEM_OPTS} ./${SERVER_NAME}-*.jar > ${STDOUT_FILE} 2>&1
java -jar ${JAVA_OPTS} ${JAVA_MEM_OPTS} ./${SERVER_NAME}-*.jar | tee -a ${STDOUT_FILE} 2>&1