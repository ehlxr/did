#!/bin/sh

echo "############## start ##############"
BUILD_DATE=`date +%Y-%m-%d:%H:%M:%S`

SERVER_NAME=`awk '/<name>[^<]+<\/name>/{gsub(/<name>|<\/name>/,"",$1);print $1;exit;}' pom.xml`
#SERVER_NAME=$(basename `pwd`)
mvn clean install -DskipTests
cp target/${SERVER_NAME}*.jar .

err=$?
echo "############## $err ##############"
if [ "$err" -ne "0" ]; then
    echo "############## build error !  ##############"
	exit 1
fi

docker_url=ehlxr/${SERVER_NAME}:latest

docker build --build-arg SERVER_NAME=${SERVER_NAME} -f ./Dockerfile -t ${docker_url} .

docker push ${docker_url}

rm ${SERVER_NAME}*.jar

echo "build & push finish ..."
echo "##############  ${BUILD_DATE}  ##############"