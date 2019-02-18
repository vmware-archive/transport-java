#!/bin/bash

RED='\033[0;31m'
LIGHTRED='\033[1;31m'
LIGHTGREEN='\033[1;32m'
LIGHTPURPLE='\033[1;35m'
ORANGE='\033[0;33m'
NC='\033[0m' # No Color

printf "\n${LIGHTPURPLE}AppFabric${NC}: ${ORANGE}Building UI${NC}\n"
cd sample-ui/
npm install > /dev/null 2>&1
npm run build:prd

printf "\n${LIGHTPURPLE}AppFabric${NC}: ${ORANGE}Building Service${NC}\n"
cd ../
cd java
./gradlew buildBootJar > /dev/null 2>&1
cd ../
printf "\n${LIGHTPURPLE}AppFabric${NC}: ${LIGHTGREEN}Build Complete${NC}\n"

