#!/bin/sh

echo "############## start ##############"
BUILD_DATE=`date +%Y-%m-%d:%H:%M:%S`

#cd ../
SERVER_NAME=`awk '/<name>[^<]+<\/name>/{gsub(/<name>|<\/name>/,"",$1);print $1;exit;}' pom.xml`
#SERVER_NAME=$(basename `pwd`)
mvn clean install -DskipTests
cp target/${SERVER_NAME}*.jar ./docker
cd docker

err=$?
echo "############## $err ##############"
if [ "$err" -ne "0" ]; then
    echo "############## build error !  ##############"
	exit 1
fi

base_url=10.19.248.200:30100
docker_url=${base_url}/ceres/${SERVER_NAME}:latest

docker build --build-arg SERVER_NAME=${SERVER_NAME} -f ./Dockerfile -t ${docker_url} .

#echo "docker login -u ggov  ${base_url}"
docker login -u ggov -p !qaz2wsX ${base_url}

docker push ${docker_url}

rm ${SERVER_NAME}*.jar

echo "build & push finish ..."
echo "##############  ${BUILD_DATE}  ##############"