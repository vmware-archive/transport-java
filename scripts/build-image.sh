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
IMAGE_TAG="${IMAGE_TAG:-latest}"

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

build_ui_docker_image() {
    local name=$1
    local tag=$2
    info "\nBuilding Docker image $name with tag $tag..."
    docker build -f $ROOT/ui/Dockerfile -t $name:$tag $ROOT/ui/
    info "\nTagging the image as $REGISTRY_PREFIX/$name:$tag..."
    docker tag $name:$tag$REGISTRY_PREFIX/$name:$tag
}

while getopts :t:n:a: flag ; do
    case $flag in
        t)
            IMAGE_TAG=$OPTARG
            ;;
        n)
            IMAGE_NAME=$OPTARG
            ;;
        a)
            DOCKER_BUILD_ARGS=$OPTARG
            ;;
        *)
            info "Usage: $0 -t tag (e.g. 1.0) -n name [-a addtional args]"
            exit 0
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z $IMAGE_NAME ] ; then
    error "Please provide image name as in $0 -t tag (e.g. 1.0) -n name [-a additional args]"
fi

build_ui_docker_image $IMAGE_NAME $IMAGE_TAG

if [ $? -eq 0 ] ; then
    echo
    echo "You can now deploy image by typing the following:"
    echo "docker push $REGISTRY_PREFIX/$IMAGE_NAME:$IMAGE_TAG"
fi
