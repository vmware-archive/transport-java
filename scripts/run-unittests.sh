#!/bin/bash
# Copyright 2018 VMware, Inc. All Rights Reserved.
#

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"

ROOT=$(cd $(dirname $0)/.. ; pwd)
PWD=$(pwd)
UI_ROOT="$PWD/sample-ui"

COLOR_RESET="\033[0m"
COLOR_RED="\033[38;5;9m"
COLOR_LIGHTCYAN="\033[1;36m"

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

usage() {
  echo 'Usage: $0 [ci|debug]'
  echo
  echo 'ci   : single-run mode'
  echo 'debug: disables sourcemaps for better debugging'
  echo
  exit 1
}

_trap() {
  error interrupted
}

if [ $(echo "$1" | grep "\-\-help\|-h") ] ; then
  usage
fi

trap '_trap' SIGINT SIGTERM

npm --prefix $UI_ROOT run lint

if [ "$1" = "ci" ] ; then
   npm --prefix $UI_ROOT run test:ci
elif [ "$1" = "debug" ] ; then
   npm --prefix $UI_ROOT run test:debug
else
   npm --prefix $UI_ROOT run test
fi

if [ $? = 0 ] ; then
    success '  ___      _ _    _   ___                       _        _ '
    success ' | _ )_  _(_) |__| | / __|_  _ __ __ ___ ___ __| |___ __| |'
    success ' | _ \ || | | / _` | \__ \ || / _/ _/ -_) -_) _` / -_) _` |'
    success ' |___/\_,_|_|_\__,_| |___/\_,_\__\__\___\___\__,_\___\__,_|'
    success

else
    warn '  ___      _ _    _   ___     _ _        _ _ '
    warn ' | _ )_  _(_) |__| | | __|_ _(_) |___ __| | |'
    warn ' | _ \ || | | / _` | | _/ _` | | / -_) _` |_|'
    warn ' |___/\_,_|_|_\__,_| |_|\__,_|_|_\___\__,_(_)'
    warn
    warn "At least one unit test case failed! See the detailed error messages above" >&2
    exit 1
fi
