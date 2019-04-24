#!/bin/bash
# Copyright 2019 VMware, Inc. All Rights Reserved.
#

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"
COLOR_LIGHTGREEN="\033[1;32m"

ROOT=$(cd $(dirname $0)/.. ; pwd)
TOOLS_FOLDER=$(cd $(dirname $0) ; pwd)

VERSION=$1
ALL_TOOLS_PRESENT=1
SKIP_DIGEST_CHECK=${SKIP_DIGEST_CHECK:-0}
TOOLS=(apigen appgen linty modelgen servgen2 specgen appomatic)

info() {
    echo -e "${COLOR_LIGHTCYAN}$1${COLOR_RESET}"
}

warn() {
    echo -e "${COLOR_RED}WARNING: $1${COLOR_RESET}"
}

success() {
    echo -e "${COLOR_LIGHTGREEN}$1${COLOR_RESET}"
}

_trap() {
    error interrupted
}

trap '_trap' SIGINT SIGTERM

compare_local_version() {
    local remote_version=$1
    for tool in ${TOOLS[@]} ; do
        if [[ ! $(sed -n -e "/${remote_version}/p" <(${TOOLS_FOLDER}/${tool} --version 2>/dev/null)) ]] ; then
            ALL_TOOLS_PRESENT=0
            info "${remote_version} not found locally. Building the tools..."
            break
        fi
    done
}

extract_tag_digest() {
    local tag=$1
    info "Pulling image and extracting digest for $tag..." 1>&2
    docker pull autogen-docker-local.artifactory.eng.vmware.com/autogen:$tag | sed -n "s/Digest: //p" | awk -F: "{print $2}"
}

info "Sewing Machine $VERSION requested"
echo
DIGEST_FILE="/tmp/.$(basename $ROOT)-sewing-machine-digest"
PREVIOUS_DIGEST=""

if [ -f "$DIGEST_FILE" ] ; then
    DIGEST_FILE_CONTENT=($(cat $DIGEST_FILE))
    PREVIOUS_DIGEST=${DIGEST_FILE_CONTENT[0]}
    PREVIOUS_VERSION=${DIGEST_FILE_CONTENT[1]}
fi

# get the digest for the given tag/version
if [ $SKIP_DIGEST_CHECK -eq 1 ] ; then
    info "Digest check skipped"
    compare_local_version "${VERSION}"
    if [ $ALL_TOOLS_PRESENT -eq 1 ] ; then
        success "Autogen tools ${VERSION} already found locally"
        exit 0
    fi
else
    LATEST_DIGEST=$(extract_tag_digest "$VERSION")

    if [ "$PREVIOUS_DIGEST" != "$LATEST_DIGEST" ] ; then
        info "Digest does not match. Building the tools..."
        ALL_TOOLS_PRESENT=0
    else
        # even if digest matches, let's make sure the actual local binaries match the version
        # specified in the digest file
        info "Checking local binaries match the last used version (${PREVIOUS_VERSION})"
        compare_local_version "${PREVIOUS_VERSION}"
    fi

    if [ $ALL_TOOLS_PRESENT -eq 1 ] ; then
        success "Autogen tools ${VERSION} already found locally"
        exit 0
    fi

    # update digest file
    echo $LATEST_DIGEST > $DIGEST_FILE
fi

docker run -i \
           --rm \
           -v $TOOLS_FOLDER:/tmp/dist \
           -e PLATFORM=$(uname -s | tr '[:upper:]' '[:lower:]') \
           autogen-docker-local.artifactory.eng.vmware.com/autogen:$VERSION

if [ $? -gt 0 ] ; then
    # if the command fails, remove the digest file
    warn "Failed to build binaries! Cleared the digest file"
    rm $DIGEST_FILE 2>/dev/null
else
    LOCAL_VERSION=$($TOOLS_FOLDER/apigen --version | head -n1 | sed -e "s/Apigen version: //" -e "s/-.*//g")
    echo $LOCAL_VERSION >> $DIGEST_FILE
    success "Sewing Machine binaries $LOCAL_VERSION were built successfully. Digest created at $DIGEST_FILE"
fi
