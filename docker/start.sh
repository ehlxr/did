#!/bin/sh

set -e

#HOSTNAME=`hostname | cut -c1-18`
LOGS_DIR="/data/logs"
GC_LOGS_DIR="/data/gc"

if [[ ! -d ${LOGS_DIR} ]]; then
  mkdir -p ${LOGS_DIR}
fi
if [[ ! -d ${GC_LOGS_DIR} ]]; then
  mkdir -p ${GC_LOGS_DIR}
fi

STDOUT_FILE=${LOGS_DIR}/${SERVER_NAME}.log
CURRENT=$(date "+%Y%m%d%H%M%S")

#JAVA_OPTS=" -Duser.timezone=\"Asia/Shanghai\" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF8"

#JAVA_MEM_OPTS=" -server -Xmx${XMX:-512m} -Xms${XMS:-128m} -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC \
#    -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=${LargePageSize:-4m} \
#    -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 \
#    -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:${GC_LOGS_DIR}/${SERVER_NAME}_${CURRENT}_gc.log \
#    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${GC_LOGS_DIR}/${SERVER_NAME}_${CURRENT}.hprof"

JAVA_MEM_OPTS=" -server -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:${GC_LOGS_DIR}/${SERVER_NAME}_${CURRENT}_gc.log \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${GC_LOGS_DIR}/${SERVER_NAME}_${CURRENT}.hprof"

echo "Starting the ${SERVER_NAME} ...\c"
chmod -R 755 ./${SERVER_NAME}*.jar
#java -jar ${JAVA_OPTS} ${JAVA_MEM_OPTS} ./${SERVER_NAME}*.jar > ${STDOUT_FILE} 2>&1
java -jar ${JAVA_MEM_OPTS} ./${SERVER_NAME}*.jar | tee -a ${STDOUT_FILE} 2>&1
