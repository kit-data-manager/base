#!/bin/sh
export VERSION=KITDM-1.4-SNAPSHOT
#For supported targets search for src/main/assembly/filter.%DEPLOY_TARGET%.properties
export DEPLOY_TARGET=release

if [ "X$1" != "X" ] ; then
    export DEPLOY_TARGET=$1
fi

#Name of the final application, KITDM by default.
export APPLICATION_WAR=KITDM.war
#Supported assembly names are: AdminUI Core
export APPLICATION_ASSEMBLY_NAME=AdminUI

# Don not edit the following lines
# ---------------------------------------------------------------------------------------
export MAVEN_OPTIONS="-DskipTests=true -DassembleMode=true -Ddeploy.target=$DEPLOY_TARGET -Dmaven.javadoc.skip=true"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

# Process result handler used to check the status of the previous build step
handle_result(){
 if [ "X$1" != "X0" ] ; then
    echo $2
    exit 1
  fi
}

echo Building distribution for release $VERSION and application $APPLICATION_WAR

echo Executing clean install
# Clean target directory 
rm -rf assembly
mkdir assembly

mvn $MAVEN_OPTIONS install -pl UserInterface
handle_result $? "Failed to compile project"

#echo Creating REST service documentation
#cd RestInterfaces 
#mvn -P generate-doc install
#handle_result $? "Failed to generate REST documentation"

#cd ..

#echo Aggregating Javadoc
#mvn $MAVEN_OPTIONS javadoc:aggregate
#handle_result $? "Failed to aggregate JavaDoc"

echo Building assembly
mvn $MAVEN_OPTIONS assembly:single -N
handle_result $? "Failed to create assembly"

echo Merging settings into $APPLICATION_WAR
cp ./assembly/$VERSION-$APPLICATION_ASSEMBLY_NAME/$VERSION/$APPLICATION_ASSEMBLY_NAME.war ./assembly/$APPLICATION_WAR
handle_result $? "Failed to copy war file to destination"

cd ./assembly/$VERSION-settings/$VERSION
jar -uvf ../../$APPLICATION_WAR WEB-INF
handle_result $? "Failed to merge settings into war file"

cd ../../../

cp src/resources/sql/schema.sql ./assembly/
handle_result $? "Failed to copy SQL schema to assembly destination"

echo Distibution package successfully created.
exit 0
