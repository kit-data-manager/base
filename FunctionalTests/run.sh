#!/bin/sh
#export MAVEN_HOME=/usr/GRID/apache-maven-3.0.3/
#export PATH=$MAVEN_HOME/bin:$PATH

#cd "$WORKSPACE"
#svn upgrade
#chmod +x makeTestDist.sh
#sh ./makeDist.sh ipejejkal
cp assembly/KITDM.war Docker/KITDMTest/tomcat
cd Docker/KITDMTest/
#shutdown just in case...
sh ./shutdown.sh
#do cleanup to remove old stuff
#sh /home/tomcat/docker-clean
#Do actual startup
sh ./startup.sh
cd ../../FunctionalTests
mvn exec:java -Dexec.mainClass="edu.kit.dama.test.TestExecutor"
cd ../Docker/KITDMTest/
sh ./shutdown.sh
