FROM postgres:9.3
MAINTAINER webmaster@datamanager.kit.edu

# Add the KIT Data Manager SQL scripts to the tmp folder for later use
COPY schema.sql /tmp/
COPY sampledata.sql /tmp/

COPY docker-entrypoint-initdb.d/init.sh /docker-entrypoint-initdb.d/init.sh
RUN chmod 0755 /docker-entrypoint-initdb.d/init.sh
