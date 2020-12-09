#!/bin/bash
# (C) 2019-2020 Apollo Foundation

SCRIPT=`realpath -s $0`
DIR=`dirname $SCRIPT`

 . ${DIR}/apl-common.sh

#"***********************************************************************"
#"* This shell script will start minting worker for mintable currencies *"
#"* Take a look at 'Mint' section in apl.properties for detailed config *"
#"***********************************************************************"

${JAVA_CMD} -jar ${APL_TOOLS_JAR} mint $@

