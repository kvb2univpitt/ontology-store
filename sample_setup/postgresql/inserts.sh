#!/usr/bin/env sh

DIR=/home/kvb2univpitt/postgresql

# ACT Project
# ##############################################################################
# create new DB user and schema
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project_schema.sql
# create metadata tables
psql postgresql://i2b2actata:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project_tables.sql
# add project description
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project.sql

# i2b2 User
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2_user.sql

# OntologyStore datasource
# ###############################################################################
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/ontstore_ds_xml.sql
psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/ontstore_i2b2hive.sql
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/ontstore_i2b2pm.sql

# i2b2
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2pm.sql
psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2hive.sql

exit 0
