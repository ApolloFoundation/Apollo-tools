#!/bin/sh
# Define versions
if [ -z "${1}" ] ; then
    NEW_VERSION=1.48.12
else
    NEW_VERSION=$1
fi
VF="VERSION"
VFtools="VERSION-tools"
# set versions in parent pom and in all childs
echo "New version is: $NEW_VERSION"
# For backward compatibility, main file is VERSION-tools
echo $NEW_VERSION > $VF
echo $NEW_VERSION > $VFtools
echo "Changing Maven's POM files"
./mvnw versions:set -DnewVersion=${NEW_VERSION}

PKG_PATH=packaging/pkg-apollo-tools.json
echo "Changing pkg-tools-blockchain.json"
sed -i -e "s/\ \"version\".*/ \"version\": \"$NEW_VERSION\",/g" ${PKG_PATH}
