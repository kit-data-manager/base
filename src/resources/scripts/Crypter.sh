#!/bin/sh
# This script is a helper script in order to de-/encrypt strings in the way KIT Data Manager does.
# It can be used e.g. to change passwords in the sampledata.sql script during the installation of 
# KIT Data Manager. For de-/encryption the global secret defined in $WEBAPP_PATH/WEB-INF/classes/datamanager.xml
# is used.

export KITDM_LOCATION=
export WEBAPP_PATH=$KITDM_LOCATION/KITDM
export LOGBACK_CONFIG=$WEBAPP_PATH/WEB-INF/classes/logback.xml
export CP_ORIG=$CLASSPATH
export LANG=en_US.iso88591

CP=.
for i in $WEBAPP_PATH/WEB-INF/lib/*.jar
do
   CP=$CP:"$i"
done

if [ -z "$CLASSPATH" ] ; then
  CLASSPATH=$CP
else
  CLASSPATH=$CP:$CLASSPATH
fi
export CLASSPATH

export CLASSPATH=.:./lib:$WEBAPP_PATH/WEB-INF/classes/:$CLASSPATH
java -Dlogback.configurationFile=$LOGBACK_CONFIG -cp $CLASSPATH edu.kit.dama.util.CryptUtil $@
EXIT=$?
export CLASSPATH=$CP_ORIG
exit $EXIT
