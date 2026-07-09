#!/bin/sh

DIR=/home/kvb2univpitt/ontology-store/sample_setup/postgresql

# Project
# ##############################################################################
# create new project schema
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_schema.sql
psql postgresql://i2b2actata:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_table.sql

# create new project
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/project.sql


# User
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/user.sql


# OntologyStore datasource
# ###############################################################################
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/ontstore_ds_xml.sql


# i2b2
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2pm.sql
psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2hive.sql

exit 0
