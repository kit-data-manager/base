#!/bin/bash

# Set the ip of the linked postgresql server within the persistence.xml
# The name of the server must be 'database' and the external port '5432'
sed -i "s/DATABASE_HOST/${DATABASE_PORT_5432_TCP_ADDR}/g" /root/WEB-INF/classes/META-INF/persistence.xml
sed -i "s/ELASTICSEARCH_HOST/${ELASTICSEARCH_PORT_9300_TCP_ADDR}/g" /root/WEB-INF/classes/datamanager.xml

# Add the newly created persistence.xml into the war file
cd /root/
jar -uvf /var/lib/tomcat7/webapps/PubRepoUI.war WEB-INF
#jar -uvf /var/lib/tomcat7/webapps/PubRepoUI_mobile.war WEB-INF
# Change the ownership back as the war file is now owned by root
chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/PubRepoUI.war
#chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/PubRepoUI_mobile.war
chown tomcat7:tomcat7 /var/archive/
chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/webdav/ -R

# Finally, start tomcat and let it deploy the war file automatically
/etc/init.d/tomcat7 start

# The container will run as long as the script is running, that's why
# we need something long-lived here
exec tail -f /var/log/tomcat7/catalina.out
