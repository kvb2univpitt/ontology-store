#!/usr/bin/env sh

DIR=/home/kvb2univpitt/oracle

# change hostname from Docker container name to localhost
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/pm_cell_data.sql

# Project
# ##############################################################################
# create new project schema
echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_schema.sql
echo exit | sqlplus i2b2actata/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_table.sql

# create new project
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/project.sql

# User
# ###############################################################################
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/user.sql

# i2b2
# ###############################################################################
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2pm.sql
echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2hive.sql

exit 0
