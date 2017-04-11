#!/bin/sh 
export KITDM_LOCATION=/home/osboxes/KITDM_TEST/test
export KITDM_VERSION=1.2-SNAPSHOT
export DB_HOST=localhost
export DB_PORT=5433
export DB_USER=postgres
export DB_PASSWORD=postgres
export DB_NAME=datamanager
export CATALINA_HOME=/var/lib/tomcat7
export TOMCAT_USER=tomcat7
export TOMCAT_GROUP=tomcat7
export REPOSITORY_STORAGE=$KITDM_LOCATION/data
export SMPT_SERVER=smtp.kit.edu

mkdir $KITDM_LOCATION
mkdir $REPOSITORY_STORAGE

sudo apt-get install -y postgresql tomcat7

echo "Unpacking binary package KITDM-$KITDM_VERSION.zip"
cp KITDM-$KITDM_VERSION.zip $KITDM_LOCATION
cd $KITDM_LOCATION
unzip KITDM-$KITDM_VERSION.zip

echo "Creating database $DB_NAME and filling initial data"
sudo -u $DB_USER createdb $DB_NAME
sudo -u $DB_USER psql -U $DB_USER -d $DB_NAME -f $KITDM_LOCATION/sql/schema.sql
sudo -u $DB_USER psql -U $DB_USER -d $DB_NAME -f $KITDM_LOCATION/sql/sampledata.sql
sudo -u $DB_USER psql -U $DB_USER -d $DB_NAME -c "INSERT INTO stagingaccesspointconfiguration VALUES (nextval('stagingaccesspointconfiguration_id_seq'), NULL, TRUE, NULL, FALSE, NULL, 'edu.kit.dama.staging.ap.impl.BasicStagingAccessPoint', '$CATALINA_HOME/webapps/webdav/', 'WebDav', 'http://localhost:8080/webdav/', FALSE, '0000-0000-0000-0000');"

sudo -u $DB_USER psql -U $DB_USER -d $DB_NAME -c "ALTER USER $DB_USER WITH PASSWORD '$DB_PASSWORD'"

#For MacOS
#sed -i .bak "s/$ARCHIVE_STORAGE/${REPOSITORY_STORAGE}/g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml
#sed -i .bak "s/$KITDM_LOCATION/${KITDM_LOCATION}/g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml
#sed -i .bak "s/$SMPT_SERVER/${SMPT_SERVER}/g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml

echo "Propagating settings to configuration files"
echo Updating datamanager.xml
sed -i "s|ARCHIVE_STORAGE|${REPOSITORY_STORAGE}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml
sed -i "s|KITDM_LOCATION|${KITDM_LOCATION}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml
sed -i "s|SMPT_SERVER|${SMPT_SERVER}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/datamanager.xml

echo Updating KITDM.xml
sed -i "s|KITDM_LOCATION|${KITDM_LOCATION}|g" $KITDM_LOCATION/webapp/KITDM.xml

echo Updating persistence.xml
sed -i "s|DB_HOST|${DB_HOST}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/META-INF/persistence.xml
sed -i "s|DB_PORT|${DB_PORT}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/META-INF/persistence.xml
sed -i "s|DB_NAME|${DB_NAME}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/META-INF/persistence.xml
sed -i "s|DB_USER|${DB_USER}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/META-INF/persistence.xml
sed -i "s|DB_PASSWORD|${DB_PASSWORD}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/META-INF/persistence.xml

echo Updating quartz_scheduler.properties
sed -i "s|DB_HOST|${DB_HOST}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/quartz_scheduler.properties
sed -i "s|DB_PORT|${DB_PORT}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/quartz_scheduler.properties
sed -i "s|DB_NAME|${DB_NAME}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/quartz_scheduler.properties
sed -i "s|DB_USER|${DB_USER}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/quartz_scheduler.properties
sed -i "s|DB_PASSWORD|${DB_PASSWORD}|g" $KITDM_LOCATION/KITDM/WEB-INF/classes/quartz_scheduler.properties

echo Updating tomcat-users.xml
sudo sed -i "s|</tomcat-users>|<role rolename=\"webdav\"/><user username=\"webdav\" password=\"webdav\" roles=\"webdav\"/></tomcat-users>|g" $CATALINA_HOME/conf/tomcat-users.xml

echo "Copying KITDM.xml to $CATALINA_HOME/conf/Catalina/localhost/"
sudo cp $KITDM_LOCATION/webapp/KITDM.xml $CATALINA_HOME/conf/Catalina/localhost/

echo "Deploying webdav servlet"
sudo cp -R $KITDM_LOCATION/webdav $CATALINA_HOME/webapps/

echo "Chaging ownership to $TOMCAT_USER:$TOMCAT_GROUP"
sudo chown $TOMCAT_USER:$TOMCAT_GROUP $CATALINA_HOME/webapps/webdav -R
sudo chown $TOMCAT_USER:$TOMCAT_GROUP $KITDM_LOCATION/KITDM -R
sudo chown $TOMCAT_USER:$TOMCAT_GROUP $REPOSITORY_STORAGE 

echo "Restarting services"
sudo service postgresql restart
sudo service tomcat7 restart
