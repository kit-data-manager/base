#!/bin/sh
cd postgres
docker build -t bare/postgres .
cd ../elasticsearch
docker build -t bare/elasticsearch .
cd ../tomcat 
docker build -t bare/tomcat .
cd ..
