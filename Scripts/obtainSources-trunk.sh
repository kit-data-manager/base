#!/bin/sh
export VERSION_NUMBER=1.5
export VERSION=KITDM-$VERSION_NUMBER
#For supported targets search for src/main/assembly/filter.%DEPLOY_TARGET%.properties

# Process result handler used to check the status of the previous build step
handle_result(){
 if [ "X$1" != "X0" ] ; then
    echo $2
    exit 1
  fi
}

# Checkout procedure for tagged version
# The build process will continue at /tmp/release_tmp to avoid conflicts while switching the another version.
export START_DIR=`pwd`

rm -rf /tmp/release_tmp
mkdir -pv /tmp/release_tmp/$VERSION/sources
cd /tmp/release_tmp

echo Checking out Tagged version $VERSION
svn co svn+ssh://ipepc21.ka.fzk.de/srv/svn/KDM/trunk

cd trunk/Utils
# From now on we are at /tmp/release_tmp/$VERSION and we can start building the release

mvn install exec:java -Dexec.workingdir="/tmp/release_tmp/$VERSION/Utils" -Dexec.mainClass="edu.kit.dama.util.release.GenerateSourceRelease" -Dexec.args="KITDM /tmp/release_tmp/trunk /tmp/release_tmp/$VERSION/sources"

cd ../../$VERSION/sources 

echo Building ZIPs
jar -cfM "$VERSION"_Source.zip *
handle_result $? "Failed to ZIP *"

echo Copying result to $START_DIR
cp "$VERSION"_Source.zip "$START_DIR"
handle_result $? "Failed to copy result to $START_DIR"

# Return to the original base directory where this script is located
cd "$START_DIR"
echo Source extraction successfully finished.

exit 0
