FROM tomcat:7-jre8
MAINTAINER webmaster@datamanager.kit.edu

RUN mkdir -p /var/archive
RUN mkdir -p /webdav/WEB-INF/
RUN mkdir -p /webdav/USERS/

COPY conf/webdav.xml /usr/local/tomcat/conf/Catalina/localhost/webdav.xml
COPY conf/KITDM.xml /usr/local/tomcat/conf/Catalina/localhost/KITDM.xml
COPY conf/ext/*.jar /usr/local/tomcat/lib/
COPY conf/webdav/WEB-INF/web.xml /webdav/WEB-INF/
