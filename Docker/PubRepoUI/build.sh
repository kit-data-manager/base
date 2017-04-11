#!/bin/sh
cd postgres
docker build -t pubrepo/postgres .
cd ../elasticsearch
docker build -t pubrepo/elasticsearch .
cd ../tomcat 
docker build -t pubrepo/tomcat .
cd ..
