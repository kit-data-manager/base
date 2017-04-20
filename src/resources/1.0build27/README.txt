KIT Data Manager Update
-----------------------

In the update package you'll find the following entries: 

|--/
|--CHANGELOG.txt
|--KDMPortlet.war
|--README.txt

For the actual deployment of the new version of KIT Data Manager please perform the following steps: 

1) Copy KDMPortlet.war to /opt/liferay/deploy
   
   $ cp KDMPortlet.war /opt/liferay/deploy
   
2) Check the deployment progress in /opt/liferay/tomcat-7.0.40/logs/catalina.out 

   $ tail -f /opt/liferay/tomcat-7.0.40/logs/catalina.out
   
3) Test your deployment.