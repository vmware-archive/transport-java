#!/bin/bash
# Copyright 2018 VMware, Inc. All Rights Reserved.
#

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"

ROOT=$(cd $(dirname $0)/.. ; pwd)
UI_ROOT="$ROOT/sample-ui"
PWD=$(pwd)
ENV=${ENV:-default}
ENV=${ENV/prd*/production}

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
  error interrupted
}

trap '_trap' SIGINT SIGTERM

info "Building for \"$ENV\" environment"

if [ "$ENV" = "default" ] ; then
    unset BUILD_PROD_FLAG
else
    BUILD_PROD_FLAG="--configuration $ENV"
fi

npm --prefix $UI_ROOT run ng -- build ${BUILD_PROD_FLAG:-}

if [ $? = 0 ] ; then
    success '  ___      _ _    _   ___                       _        _ '
    success ' | _ )_  _(_) |__| | / __|_  _ __ __ ___ ___ __| |___ __| |'
    success ' | _ \ || | | / _` | \__ \ || / _/ _/ -_) -_) _` / -_) _` |'
    success ' |___/\_,_|_|_\__,_| |___/\_,_\__\__\___\___\__,_\___\__,_|'
    success
    success "UI artifacts were successfully created under dist/sample-ui"
else
    warn '  ___      _ _    _   ___     _ _        _ _ '
    warn ' | _ )_  _(_) |__| | | __|_ _(_) |___ __| | |'
    warn ' | _ \ || | | / _` | | _/ _` | | / -_) _` |_|'
    warn ' |___/\_,_|_|_\__,_| |_|\__,_|_|_\___\__,_(_)'
    warn
    warn "Failed to build! See the detailed error messages above" >&2
    exit 1
fi
