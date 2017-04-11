#!/bin/sh
#For trunk build the actual version number is determined later (line 44) automatically
#export VERSION_NUMBER=trunk
#export VERSION=KITDM-$VERSION_NUMBER
#For supported targets search for src/main/assembly/filter.%DEPLOY_TARGET%.properties
export DEPLOY_TARGET=release

if [ "X$1" != "X" ] ; then
    export DEPLOY_TARGET=$1
fi

export TEST_SKIP=false
export JAVADOC_SKIP=false

if [ "X$2" == "Xquick" ] ; then
   export TEST_SKIP=true
   export JAVADOC_SKIP=true
fi

echo Build Properties 
echo Skipping tests: $TESTS_SKIP
echo Skipping JavaDoc Generation: $JAVADOC_SKIP

export SVN_USER=`whoami`

if [ "X$3" != "X" ] ; then
   export SVN_USER=$3
fi

#Name of the final application, KITDM by default.
export APPLICATION_WAR=KITDM.war
#Supported assembly names are: AdminUI Core
export APPLICATION_ASSEMBLY_NAME=AdminUI

# Don not edit the following lines
# ---------------------------------------------------------------------------------------
export MAVEN_OPTIONS="-DskipTests=$TEST_SKIP -DassembleMode=true -Ddeploy.target=$DEPLOY_TARGET -Dmaven.javadoc.skip=$JAVADOC_SKIP"
export MAVEN_OPTS="$MAVEN_OPTS -Xmx1024m"

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
mkdir /tmp/release_tmp
cd /tmp/release_tmp

echo Checking out trunk as user $SVN_USER
svn co svn+ssh://$SVN_USER@ipepc21.ka.fzk.de/srv/svn/KDM/trunk

cd trunk
# From now on we are at /tmp/release_tmp/trunk and we can start building the release

#Determine the current version number
export VERSION_NUMBER=$(mvn -q \
    -Dexec.executable="echo" \
    -Dexec.args='${project.version}' \
    --non-recursive \
    org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
export VERSION=KITDM-$VERSION_NUMBER

echo Building distribution for release $VERSION and application $APPLICATION_WAR

echo Executing clean install
# Clean target directory 
rm -rf assembly
mkdir assembly

mvn $MAVEN_OPTIONS clean install
handle_result $? "Failed to compile project"

echo Creating REST service documentation
cd RestInterfaces 
mvn -P generate-doc install
handle_result $? "Failed to generate REST documentation"

cd ..

echo Aggregating Javadoc
mvn $MAVEN_OPTIONS javadoc:aggregate
handle_result $? "Failed to aggregate JavaDoc"

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
  
echo Assembly phase finished. Building distribution package.

mkdir -pv assembly/package/KITDM
mkdir -pv assembly/package/sql
mkdir -pv assembly/package/tomcat-ext

cp assembly/$APPLICATION_WAR assembly/package/KITDM/
handle_result $? "Failed to copy $APPLICATION_WAR to package location"

cp src/resources/tomcat-ext/*.jar assembly/package/tomcat-ext/
handle_result $? "Failed to extract tomcat-ext jars to package location"

cd assembly/package/KITDM

echo Extracting $APPLICATION_WAR
jar -xf $APPLICATION_WAR
handle_result $? "Failed to extract $APPLICATION_WAR"

echo Removing $APPLICATION_WAR
rm $APPLICATION_WAR

cd ../../..

# Copying of additional files, settings, documentation and building of the final zip archive
echo Copying webapp folder
cp -R src/resources/webapp assembly/package/
handle_result $? "Failed to copy webapp folder"

echo Copying webdav folder
cp -R src/resources/webdav assembly/package/
handle_result $? "Failed to copy webdav folder"

echo Copying base sql scripts folder
cp src/resources/sql/schema.sql assembly/package/sql/
handle_result $? "Failed to copy schema.sql"

cp src/resources/sql/helpful_views.sql assembly/package/sql/
handle_result $? "Failed to copy helpful_views.sql"

echo Copying documentation folder
cp -R assembly/$VERSION-documentation/doc assembly/package/
handle_result $? "Failed to copy documentation"

echo Copying simon folder
cp -R src/resources/simon assembly/package/
handle_result $? "Failed to copy SiMon configuration files"

echo Copying scripts folder
cp -R src/resources/scripts assembly/package/
handle_result $? "Failed to copy Cron scripts"

echo Copying CHANGELOG and README
cp src/resources/1.1/*.txt assembly/package/
#This might not exist for trunk builds, so use the 1.1 files
handle_result $? "Failed to copy CHANGELOG and README"

echo Copying SQL update script
cp src/resources/1.1/update/*.sql assembly/package/sql/
#This might not exist for trunk builds, so use the 1.1 files
handle_result $? "Failed to copy SQL update script"

cd assembly/package

echo Building ZIPs
jar -cfM $VERSION.zip `ls`
handle_result $? "Failed to ZIP release package"

cd doc
jar -cfM ../"$VERSION"_Documentation.zip apidoc manual rest
handle_result $? "Failed to ZIP documentation"

echo Copying result to $START_DIR
cp ../*.zip "$START_DIR"
handle_result $? "Failed to copy result to $START_DIR"

# Return to the original base directory where this script is located
cd "$START_DIR"
echo Packaging successfully finished.

exit 0
