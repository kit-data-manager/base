#!/bin/sh
#Helper script for KIT Data Manager supporting while updating KIT Data Manager to a new version.
#The tool wrapped by the script compares two library folders (typically WEB-INF/lib of two 
#versions of KIT Data Manager), determines the versions of the contained jar files and prints out
#changes (updates, deletes, new libraries) between both folders. Optionally, a script can be generated
#that merges the "old version" folder into the on of the "new version". 

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
echo Performing Library Comparison with Arguments $@
java -Dlogback.configurationFile=$LOGBACK_CONFIG -cp $CLASSPATH edu.kit.dama.util.update.UpdateClient $@
EXIT=$?
export CLASSPATH=$CP_ORIG
echo Terminating Library Compare
exit $EXIT
