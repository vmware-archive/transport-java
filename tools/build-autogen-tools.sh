#!/bin/bash -e
TOOLS_FOLDER=$(cd $(dirname $0) ; pwd)
TOOLS=(apigen appgen2 linty modelgen servgen2 specgen appomatic )
ALL_TOOLS_PRESENT=1
VERSION=$1

for tool in ${TOOLS[@]} ; do
    if [[ ! $(sed -n -e "/${VERSION}/p" <(${TOOLS_FOLDER}/${tool} --version 2>/dev/null)) ]] ; then
        ALL_TOOLS_PRESENT=0
        echo "${VERSION} not found locally. Building the tools..."
        break
    fi
done

if [ $ALL_TOOLS_PRESENT -eq 1 ] ; then
    echo "Autogen tools ${VERSION} already found locally"
    exit 0
fi

docker run -i \
           --rm \
           -v $TOOLS_FOLDER:/tmp/autogen-dist \
	   autogen-docker-local.artifactory.eng.vmware.com/autogen:$VERSION \
	   -a amd64 \
	   -o $(uname -s | tr '[:upper:]' '[:lower:]')


