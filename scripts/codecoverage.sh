#!/bin/bash

ROOT=$(cd $(dirname $0)/.. ; pwd)

# core bifrost coverage
CORE_BIFROST=($(cat $ROOT/java/build/reports/jacoco/test/html/index.html | \
    grep -oE '(([0-9]+,?)*[0-9]+) of (([0-9]+,?)*[0-9]+)' | \
    sed -e 's/,//g' | sed -En 's/([0-9]+) of ([0-9]+)/\1 \2/p' | \
    head -1))

BIFROST_CSP=($(cat $ROOT/java/csp/build/reports/jacoco/test/html/index.html | \
    grep -oE '(([0-9]+,?)*[0-9]+) of (([0-9]+,?)*[0-9]+)' | \
    sed -e 's/,//g' | sed -En 's/([0-9]+) of ([0-9]+)/\1 \2/p' | \
    head -1))

AGGREGATED_UNCOVERED_LINES="$[${CORE_BIFROST[0]} + ${BIFROST_CSP[0]}]"
AGGREGATED_TOTAL_LINES="$[${CORE_BIFROST[1]} + ${BIFROST_CSP[1]}]"

echo Code coverage
echo ================
echo Core Bifrost: $(bc <<< "scale = 2; (100 - 100 * ${CORE_BIFROST[0]} / ${CORE_BIFROST[1]})")%
echo Bifrost CSP: $(bc <<< "scale = 2; (100 - 100 * ${BIFROST_CSP[0]} / ${BIFROST_CSP[1]})")%
echo
echo Total test coverage:$(bc <<< "scale = 2; (100 - 100 * $AGGREGATED_UNCOVERED_LINES / $AGGREGATED_TOTAL_LINES)")%
