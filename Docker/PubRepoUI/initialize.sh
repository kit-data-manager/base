#!/bin/sh
export ARCHIVE_SHARE=`pwd`/share/archive/
export CACHE_SHARE=`pwd`/share/cache/
export LOG_SHARE=`pwd`/share/log/

docker run -d -p 5432:5432 --name database pubrepo/postgres
docker run -d -p 9200:9200 -p 9300:9300 --name elasticsearch pubrepo/elasticsearch
docker run -d -p 8889:8080 --link database:database --link elasticsearch:elasticsearch -v $ARCHIVE_SHARE:/var/archive/ -v $CACHE_SHARE:/var/lib/tomcat7/webapps/webdav/ -v $LOG_SHARE:/var/log/tomcat7/ --name tomcat pubrepo/tomcat
