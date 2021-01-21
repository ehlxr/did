#!/bin/sh

set -e

echo "############## start ##############"
BUILD_DATE=`date +%Y-%m-%d:%H:%M:%S`

#CURR_DIR=`basename $PWD`
#if [[ ${CURR_DIR} != "docker" ]]; then
#    echo "############## must exec in docker dir ##############"
#    exit 2;
#fi
#
#cd ../
#SERVER_NAME=`awk '/<name>[^<]+<\/name>/{gsub(/<name>|<\/name>/,"",$1);print $1;exit;}' pom.xml`
#SERVER_NAME=$(basename `pwd`)
SERVER_NAME=${MODULE_PARMS}

SERVER_JAR=$PWD/${MODULE_PARMS}/target/${SERVER_NAME}*.jar
#if [ ! -n "ls ${SERVER_JAR} >/dev/null 2>&1" ]; then
if [[ "`echo ${SERVER_JAR}`" != "${SERVER_JAR}" ]]; then
    echo exist ${SERVER_JAR} skip maven build.
else
    mvn clean install -DskipTests
fi

cp ${SERVER_JAR} ./docker
#cd docker

if [[ ${DOCKER_IMAGE_TAG} ]];then
    image_tag=${DOCKER_IMAGE_TAG}
else
    echo "############## DOCKER_IMAGE_TAG is null !  ##############"
    # image_tag=$(git symbolic-ref --short -q HEAD)
    image_tag=`date "+%Y%m%d_%H%M%S"`
fi
echo "############## image_tag is: ${image_tag} ##############"

base_url=docker.ehlxr.me
docker_url=${base_url}/ehlxr/${SERVER_NAME}:${image_tag}

docker build --build-arg SERVER_NAME=${SERVER_NAME} -f ./docker/Dockerfile -t ${docker_url} .
# get user pwd from environment
echo "${DOCKER_REGISTRY_PWD}" | docker login --username ${DOCKER_REGISTRY_USER} --password-stdin ${base_url}

docker push ${docker_url}

rm ./docker/${SERVER_NAME}*.jar
echo "############## build & push finish: ${BUILD_DATE}  ##############"

err=$?
if [[ "$err" -ne "0" ]]; then
    echo "############## build error !  ##############"
	exit 1
fi