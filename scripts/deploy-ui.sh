#!/bin/bash
# Copyright 2019 VMware, Inc. All Rights Reserved.

ROOT=$(cd $(dirname $0)/.. ; pwd)
PWD=$(pwd)
REGISTRY_PREFIX=${REGISTRY_PREFIX:-}
COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"
COLOR_LIGHTGREEN="\033[1;32m"
TARGET_TAG="${TARGET_TAG:-latest}"

error() {
    echo -e "${COLOR_RED}ERROR: $1${COLOR_RESET}" >&2
    exit 1
}

warn() {
    echo -e "${COLOR_RED}WARNING: $1${COLOR_RESET}"
}

info() {
    echo -e "${COLOR_LIGHTCYAN}$1${COLOR_RESET}"
}

success() {
    echo -e "${COLOR_LIGHTGREEN}$1${COLOR_RESET}"
}

_trap() {
  echo interrupted >&2
  rm $ROOT/scripts/deploy.sh 2>/dev/null
  exit 1
}

trap '_trap' SIGINT SIGTERM

# prepare the script
cat <<EOT > $ROOT/scripts/deploy.sh
#!/bin/bash
COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"
COLOR_LIGHTGREEN="\033[1;32m"

error() {
    echo -e "${COLOR_RED}ERROR: \$1${COLOR_RESET}" >&2
    exit 1
}

warn() {
    echo -e "${COLOR_RED}WARNING: \$1${COLOR_RESET}"
}

info() {
    echo -e "${COLOR_LIGHTCYAN}\$1${COLOR_RESET}"
}

success() {
    echo -e "${COLOR_LIGHTGREEN}\$1${COLOR_RESET}"
}

# stop sample-ui container
echo
info "Stopping sample-ui..."
docker rm -f sample-ui 2>/dev/null
if [ \$? -gt 0 ] ; then
    info "sample-ui not running"
else
    success "sample-ui stopped and removed"
fi

# start sample-ui container
echo
info "Starting sample-ui using tag ${TARGET_TAG}..."
docker run --name sample-ui -d -p 80:80 ${REGISTRY_PREFIX}/sample-ui:${TARGET_TAG}

# check if container is running normally
echo
info "Checking if container is running..."
sleep 5
docker top sample-ui >/dev/null 2>&1
if [ \$? -gt 0 ] ; then
    docker logs sample-ui
    error "Nginx failed to start! See the logs above"
fi
success "Deployment was successful"
EOT

set +e
info "Running deploy script remotely..."
sshpass -p "${UI_HOST_PASSWORD}" scp -o StrictHostKeyChecking=no $ROOT/scripts/deploy.sh ${UI_HOST_USERNAME}@${UI_HOST}:/tmp/
sshpass -p "${UI_HOST_PASSWORD}" ssh -o StrictHostKeyChecking=no ${UI_HOST_USERNAME}@${UI_HOST} "chmod +x /tmp/deploy.sh && bash /tmp/deploy.sh"
rm $ROOT/scripts/deploy.sh
set -e

echo
info "Checking if Container is up and running..."
wget -O- --retry-connrefused --waitretry=5 -t 15 http://appfabric.eng.vmware.com > /dev/null 2>&1
if [ $? -gt 0 ] ; then
    error "Container did not start normally"
fi
success "Container is up and running"
