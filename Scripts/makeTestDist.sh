#!/bin/sh
# Special version of the makeDist script that is used for functional testing builds. 

# Do not edit the following lines
# ---------------------------------------------------------------------------------------
#For supported targets search for src/main/assembly/filter.%DEPLOY_TARGET%.properties
export DEPLOY_TARGET=docker
#Version is determined later automatically
#export VERSION=KITDM-1.2-SNAPSHOT
export MAVEN_OPTIONS="-DskipTests=true -DassembleMode=true -Ddeploy.target=$DEPLOY_TARGET"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
#Name of the final application, KITDM by default.
export APPLICATION_WAR=KITDM.war
#Supported assembly names are: AdminUI Core
export APPLICATION_ASSEMBLY_NAME=Test

#Determine the current version number
export VERSION_NUMBER=$(mvn -q \
    -Dexec.executable="echo" \
    -Dexec.args='${project.version}' \
    --non-recursive \
    org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
export VERSION=KITDM-$VERSION_NUMBER

export RELEASE_NAME=$VERSION

echo Building distribution for release $RELEASE_NAME and application $APPLICATION_WAR

echo Executing clean install
# Clean target directory 
rm -rf assembly
mkdir assembly

mvn $MAVEN_OPTIONS install -P FunctionalTest
  if [ "X$?" != "X0" ] ; then
    echo Failed to call clean install
    exit 1
  fi

echo Building assembly
mvn $MAVEN_OPTIONS assembly:single -N -P FunctionalTest

  if [ "X$?" != "X0" ] ; then
    echo Failed to create assembly
    exit 1
  fi

#echo Merging settings into $APPLICATION_WAR
cd ./assembly/$RELEASE_NAME-$APPLICATION_ASSEMBLY_NAME/$RELEASE_NAME
zip -r ../../$APPLICATION_WAR *

  if [ "X$?" != "X0" ] ; then
    echo Failed to copy war file to destination
    exit 1
  fi

cd ../../../

cp ./src/resources/sql/schema.sql ./assembly/

  if [ "X$?" != "X0" ] ; then
    echo Failed to copy SQL schema to assembly destination
    exit 1
  fi


echo Distibution package successfully created.
exit 0
