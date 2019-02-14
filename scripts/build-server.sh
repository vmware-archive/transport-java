#!/bin/bash
# Copyright 2019 VMware, Inc. All Rights Reserved.
#

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"

ROOT=$(cd $(dirname $0)/.. ; pwd)
PWD=$(pwd)

IMAGES=(server_sizerdb server_sizerbackend)

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

_trap() {
  echo interrupted >&2
  exit 1
}

success() {
    echo -e "${COLOR_LIGHTGREEN}$1${COLOR_RESET}"
}

trap '_trap' SIGINT SIGTERM

info "Building server..."
$ROOT/java/gradlew -p $ROOT/java clean buildBootJar

if [ $? -gt 0 ] ; then
    warn '  ___      _ _    _   ___     _ _        _ _ '
    warn ' | _ )_  _(_) |__| | | __|_ _(_) |___ __| | |'
    warn ' | _ \ || | | / _` | | _/ _` | | / -_) _` |_|     (ノಠ益ಠ)ノ彡┻━┻'
    warn ' |___/\_,_|_|_\__,_| |_|\__,_|_|_\___\__,_(_)'
    warn
    warn "Failed to build the server! See the detailed error messages above" >&2
    exit 1
fi
