#!/bin/sh
#For supported targets search for src/main/assembly/filter.%DEPLOY_TARGET%.properties
export DEPLOY_TARGET=release
#Name of the final application, KITDM by default.
export APPLICATION_WAR=KITDM.war
#Supported assembly names are: AdminUI Core
export APPLICATION_ASSEMBLY_NAME=AdminUI

# Do not edit the following lines
# ---------------------------------------------------------------------------------------
if [ "X$1" != "X" ] ; then
    export VERSION_NUMBER=$1
fi

export VERSION=KITDM-$VERSION_NUMBER

export MAVEN_OPTS="-DskipTests=true -DassembleMode=true -Ddeploy.target=$DEPLOY_TARGET -Dmaven.javadoc.skip=true"

# Process result handler used to check the status of the previous build step
handle_result(){
 if [ "X$1" != "X0" ] ; then
    echo $2
    exit 1
  fi
}

# This script requires that previously makeDist-$VERSION.sh has been executed.
export START_DIR=`pwd`

cd /tmp/release_tmp/trunk
handle_result $? "Failed to change directory to release location. Please execute makeDist-trunk.sh first."
# From now on we are at /tmp/release_tmp/trunk and we can start building the release

mkdir -pv assembly/update/KITDM
mkdir -pv assembly/update/sql
handle_result $? "Failed to create update directory structure."

echo Copying KITDM folder
cp -R assembly/package/KITDM/* assembly/update/KITDM/
handle_result $? "Failed to copy KIT DM package"

echo Deleting settings files
rm assembly/update/KITDM/WEB-INF/classes/logback.xml
rm assembly/update/KITDM/WEB-INF/classes/datamanager.xml
rm assembly/update/KITDM/WEB-INF/classes/META-INF/persistence.xml
rm assembly/update/KITDM/WEB-INF/classes/quartz*

echo Copying updated txt resources
cp src/resources/$VERSION_NUMBER/*.txt assembly/update/
handle_result $? "Failed to copy updated txt resources from release"

echo Copying updated txt resources
cp src/resources/$VERSION_NUMBER/update/*.txt assembly/update/
handle_result $? "Failed to copy updated txt resources from update"

echo Copying SQL scripts
handle_result $? "Failed to create SQL target directory"
cp src/resources/$VERSION_NUMBER/update/*.sql assembly/update/sql/
handle_result $? "Failed to copy SQL scripts"

cd assembly/update

echo Building ZIPs
jar -cfM $VERSION"_Update.zip" README.txt CHANGELOG.txt KITDM sql
handle_result $? "Failed to build zip package"

cp $VERSION"_Update.zip" $START_DIR
echo Update packaging successfully finished.

cd $START_DIR

exit 0