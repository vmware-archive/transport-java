#!/bin/bash
# Copyright 2018 VMware, Inc. All Rights Reserved.
#
set -eo pipefail

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"

ENV=${ENV:-dev}
ENV=${ENV/dev*/dev}
ENV=${ENV/prod*/prd}
ENV=${ENV/staging*/stg}
IMAGE_TAG="${CI_PIPELINE_ID:-latest}"

REGISTRY_PREFIX=${REGISTRY_PREFIX:-appfabric-service-docker-local.artifactory.eng.vmware.com}
ROOT=$(cd $(dirname $0)/.. ; pwd)
PWD=$(pwd)

error() {
    echo -e "${COLOR_RED}Error! $1${COLOR_RESET}" >&2
    exit 1
}

info() {
    echo -e "${COLOR_LIGHTCYAN}$1${COLOR_RESET}"
}

build_service_docker_image() {
    local prefix=$1
    local tag=$2
    info "\nBuilding Docker image ${prefix}-service with tag $tag..."
    docker build -f $ROOT/java/Dockerfile -t ${prefix}-service:$tag $ROOT/java/
    info "\nTagging the image as $REGISTRY_PREFIX/${prefix}-service:$tag..."
    docker tag ${prefix}-service:$tag $REGISTRY_PREFIX/${prefix}-service:$tag
}

build_ui_docker_image() {
    local prefix=$1
    local tag=$2
    info "\nBuilding Docker image ${prefix}-ui with tag $tag..."
    docker build -f $ROOT/sample-ui/Dockerfile -t ${prefix}-ui:$tag $ROOT/sample-ui/
    info "\nTagging the image as $REGISTRY_PREFIX/${prefix}-ui:$tag..."
    docker tag ${prefix}-ui:$tag $REGISTRY_PREFIX/${prefix}-ui:$tag
}

while getopts :t:p:a: flag ; do
    case $flag in
        t)
            IMAGE_TAG=$OPTARG
            ;;
        p)
            APP_PREFIX=$OPTARG
            ;;
        a)
            DOCKER_BUILD_ARGS=$OPTARG
            ;;
        *)
            info "Usage: $0 -t tag -p image_name_prefix [-a addtional args]"
            exit 0
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z $IMAGE_TAG ] ; then
    error "Image tag is missing.\nUsage: $0 -t tag -p image_name_prefix [-a addtional args]"
fi

if [ -z $APP_PREFIX ] ; then
    error "Image prefix is missing.\nUsage: $0 -t tag -p image_name_prefix [-a addtional args]"
fi

build_ui_docker_image $APP_PREFIX $IMAGE_TAG
build_service_docker_image $APP_PREFIX $IMAGE_TAG

if [ $? -eq 0 ] ; then
    echo
    echo "You can now deploy the images by typing the following:"
    echo "docker push $REGISTRY_PREFIX/$APP_PREFIX-ui:$IMAGE_TAG"
    echo "docker push $REGISTRY_PREFIX/$APP_PREFIX-service:$IMAGE_TAG"
fi
