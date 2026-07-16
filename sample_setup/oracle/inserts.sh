#!/usr/bin/env sh

DIR=/home/kvb2univpitt/oracle

# change hostname from Docker container name to localhost
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/pm_cell_data.sql

# Project
# ##############################################################################
# create new project schema
echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project_schema.sql
echo exit | sqlplus i2b2actata/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project_tables.sql
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project.sql

# i2b2 User
# ###############################################################################
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2_user.sql

# OntologyStore datasource
# ###############################################################################
echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/ontstore_ds_xml.sql
echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/ontstore_i2b2hive.sql
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/ontstore_i2b2pm.sql

# i2b2
# ###############################################################################
echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2hive.sql

exit 0
