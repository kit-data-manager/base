FROM ubuntu
MAINTAINER support@kitdatamanager.net

# Install JDK 7 and Tomcat 7. --no-install-recommends is a workaround to avoid installing fuse together with JDK 7 
# as this causes an error during installation.
RUN apt-get update
RUN apt-get install -y --no-install-recommends openjdk-7-jdk tomcat7

# Create the repository archive folder and set the ownership to tomcat7:tomcat7
RUN mkdir /var/archive/
RUN chown tomcat7:tomcat7 /var/archive/

# 'Deploy' the webdav application, add the prepared user database and change the ownership
RUN mkdir /var/lib/tomcat7/webapps/webdav
RUN mkdir /var/lib/tomcat7/webapps/webdav/WEB-INF
ADD conf/webdav/WEB-INF/web.xml /var/lib/tomcat7/webapps/webdav/WEB-INF/web.xml
ADD conf/tomcat-users.xml /etc/tomcat7/
RUN chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/webdav /etc/tomcat7/tomcat-users.xml -R

# Add the preliminary persistence.xml which is later on modified via the run.sh script in order
# to set the proper IP of the linked postgres database container.
RUN mkdir -p /root/WEB-INF/classes/META-INF/
ADD conf/persistence.xml /root/WEB-INF/classes/META-INF/persistence.xml
ADD conf/datamanager.xml /root/WEB-INF/classes/datamanager.xml
ADD conf/logback.xml /root/WEB-INF/classes/logback.xml

VOLUME  ["/var/log/tomcat7","/var/archive","/var/lib/tomcat7/webapps/webdav/"]

RUN chown tomcat7:tomcat7 /var/archive/
RUN chown tomcat7:tomcat7 /var/log/tomcat7/
RUN chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/webdav/

# Expose port 8080 and define the application to start with the container
EXPOSE 8080
CMD ["/root/run.sh"]

# Add the start script and make it executable
ADD conf/run.sh /root/run.sh
RUN chmod +x /root/run.sh

# Add the war file to deploy and change the ownership to tomcat7:tomcat7
ADD PubRepoUI.war /var/lib/tomcat7/webapps/
RUN chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/PubRepoUI.war
