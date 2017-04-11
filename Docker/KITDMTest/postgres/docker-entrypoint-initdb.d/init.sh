#!/bin/bash

psql -U $POSTGRES_USER -d $POSTGRES_DB -f /tmp/schema.sql
psql -U $POSTGRES_USER -d $POSTGRES_DB -f /tmp/sampledata.sql
