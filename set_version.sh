#!/bin/sh
# Define versions
if [ -z "${1}" ] ; then
    NEW_VERSION=1.47.4
else
    NEW_VERSION=$1
fi
VF="VERSION"
# set versions in parent pom and in all childs
echo "New version is: $NEW_VERSION"
echo $NEW_VERSION > $VF
echo "Changing Maven's POM files"
./mvnw versions:set -DnewVersion=${NEW_VERSION}

# set verions in Constants.java (application hardcoded version)
CONST_PATH=apl-utils/src/main/java/com/apollocurrency/aplwallet/apl/util/Constants.java
echo "Changing Constants in $CONST_PATH"
PKG_PATH=packaging/pkg-apollo-tools.json
echo "Changing pkg-tools-blockchain.json"
sed -i -e "s/\ \"version\".*/ \"version\": \"$NEW_VERSION\",/g" ${PKG_PATH}
