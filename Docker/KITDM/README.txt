This folder contains a generic Docker setup for running KIT DM instances. It allows to run a KIT DM based repository instance packed inside a  
WAR file out of the box without any additional software except Docker and Docker Compose. Please follow the steps decribed below in order to 
get your first repository instance running.

Prerequisites
-------------

Docker 1.10+
Docker Compose 1.8.0+

Content
-------

docker-compose.yml - Docker Compose file defining containers, volumes and open ports
elasticsearch - Folder containing Dockerfile and configuration for the elasticsearch container
postgres - Folder containing Dockerfile, schema and sampledata for the postgres database
scripts - Folder containing helpful scripts, e.g. for backup and restore of volumes
share - Folder containing subfolders (log and webapps) that are shared with Docker
tomcat - Folder containing Dockerfile, configuration and additional libraries for the tomcat container

Usage
-----

1. Copy your KITDM.war to share/webapps/KITDM/
2. Execute `docker-compose build` to download all Docker containers
3. Execute `docker volume create --name=archive` to create the volume for storing the archived data of the repository system
4. Execute `docker volume create --name=cache` to create the volume for storing the cached data of the repository system 
5. Execute `docker-compose up -d` to startup all Docker containers in the background. If you start the containers the first time skip the -d argument to see all outputs.

Now, four containers named 'tomcat', 'database', 'elasticsearch' and 'rabbitmq' will startup. If the startup has finished, the AdminUI of the KIT DM instance is accessible 
via http://localhost:8889/KITDM

Username/password are 'admin'/'dama14' (without quotes), the KIT DM instance has one WebDav access point configured available at http://localhost:8889/webdav/, again with 
username 'admin' and password 'dama14'. To check the WebDav access you can open http://localhost:8889/webdav/USERS in your browser. 

From outside the containers you have access to all logfiles located at '/usr/local/tomcat/logs' inside the container via 'share/logs'. All other data is either transient or 
located in volumes 'cache' and 'archive' To redeploy the KIT DM instance, e.g. during development, just replace the WAR file located at 'share/webapps/KITDM/KITDM.war' by
the new version. The tomcat instance in the according Docker container will detect the change and performs a reload.

If you want to stop all containers you just have to execute `docker-compose stop`. If you want to remove stopped containers call `docker-compose rm` to remove all containers.

Attention: Removing containers won't remove the shared volumes 'archive' and 'cache'. If you want to remove them, you have to call `docker volume rm archive` and `docker volume rm cache`. 
Otherwise, all data within the volumes is persistent.

Useful Commands
---------------

docker exec -ti database /bin/bash 
   - Login to the database (or any other) container. Inside the container you may connect to the postgres server via psql -U docker -h localhost -W -d datamanager and perform further queries.
     The password is 'docker'.

docker logs tomcat
   - Show the stdout/stderr console of the tomcat (or any other) container. Basically, the output is the same as the output of the `docker-compose up` command if you omit the -d argument.

docker volume inspect cache 
   - Show information about the 'cache' volume (also possible for 'archive'). The info contains a 'Mountpoint' element pointing to the local folder where the volume data 
     is located. Attention: In some cases, e.g. in Docker on MacOS, the Mountpoint may refer to a local folder that does not exists. Here, viewing the content of a volume is not possible that easy.

docker volume rm cache 
   - Remove the cache volume (also possible for 'archive'). After removal the volume has to be created again before the next startup.

Scripts
--------

scripts/backup.sh
   - Can be used to backup a volume by its mountpoint in a container to a file named 'backup.tar' located in the current directory. Usage: backup.sh <CONTAINER> <VOLUME> (e.g. backup.sh tomcat /var/archive)

scripts/restore.sh
   - Can be used to restore a backup from a file named 'backup.tar' into the provided container. Usage: restore.sh <CONTAINER> (e.g. restore.sh tomcat)

scripts/backupDB.sh
   - Can be used to backup the postgres database in a container to a file named 'database.sql' in the current directory. Usage: backupDB.sh <CONTAINER> (e.g. backupDB.sh database)

scripts/cleanup.sh
   - Performs `docker-compose stop` followed by `docker-compose rm` and omits all errors, e.g. if no container is running. Usage: cleanup.sh 
