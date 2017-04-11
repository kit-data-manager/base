#!/bin/sh

export DATAMANAGER_PATH=/usr/local/tomcat/webapps/KITDM/
export LOGBACK_CONFIG=/usr/local/tomcat/webapps/KITDM/WEB-INF/classes/logback.xml
export CP_ORIG=$CLASSPATH
CP=.
for i in $DATAMANAGER_PATH/WEB-INF/lib/*.jar
do
   CP=$CP:"$i"
done

if [ -z "$CLASSPATH" ] ; then
  CLASSPATH=$CP
else
  CLASSPATH=$CP:$CLASSPATH
fi
export CLASSPATH

export CLASSPATH=.:./lib:$DATAMANAGER_PATH/WEB-INF/classes/:$CLASSPATH
echo Performing Transfer Finalizer with Arguments $@
echo Finalizing transfers
java -Dlogback.configurationFile=$LOGBACK_CONFIG -cp $CLASSPATH edu.kit.dama.staging.util.TransferFinalizer $@
EXIT=$?
  
export CLASSPATH=$CP_ORIG
echo Terminating Transfer Finalizer
exit $EXIT
