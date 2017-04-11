KIT Data Manager Update
-----------------------

In the update package you'll find the following entries: 

|--/
|--documentation
   |--manual
   |--rest
|--deploy.sql
|--KDMPortlet.war
|--README.txt

The documentation folder contains the very first version of the KIT Data Manager manual as well as the updated documentation for all ReST services. 
You can copy the entire content to the folder '/var/www/' in your Virtual Machine in order to have the correct documentation available in there.

For the actual deployment of the new version of KIT Data Manager please perform the following steps: 

1) Stop the Tomcat server
   
   $ sudo /etc/init.d/tomcat7 stop
   
2) Check your Java version. 

   $ java -version
   
2.1) If your Java installation is not version 1.7, please update Java using the following commands:

   $ sudo apt-get intsall openjdk-7-jre
   $ sudo update-alternatives --config java
   
   The second command gives you a list of installed Java versions. Please select the entry for version 1.7 in order to select Java 1.7.

3) Some tables in the PostgreSQL database have changed. In order to perform an update, execute the following steps.

   $ pg_dump -U postgres datamanager > datamanager.sql
   $ psql -U postgres -d datamanager -f update.sql
  
   The first call created a backup of your database, just in case anything goes wrong. The second call applies the schema update.
   
   Attention: If you have added any AccessMethods for staging, they will be gone after the update and currently there will be no easy way to add them again.
              Please contact me if you need custom AccessMethods.   
 
4) Restart the Tomcat server
 
    $ sudo /etc/init.d/tomcat7 start
 
5) Deploy the new version of the KIT Data Manager portlet

   $ cp KDMPortlet.war /opt/liferay/deploy
   
6) Monitor the deployment process in /opt/liferay/tomcat-7.0.40/logs/catalina.out

   $ tail -f /opt/liferay/tomcat-7.0.40/logs/catalina.ou

7) Test your deployment.