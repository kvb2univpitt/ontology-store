#!/usr/bin/env sh

DIR=/home/kvb2univpitt/test_cases/oracle

# Docker DB
# ##############################################################################
# change hostname from Docker container name to localhost
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/pm_cell_data.sql

# ACT Project
# ##############################################################################
# create new DB user and schema
echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project_schema.sql
# add tables to project schema
echo exit | sqlplus i2b2actata/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project_tables.sql
# add project description
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2actdata_project.sql

# Add i2b2 Demo User to ACT Project
# ###############################################################################
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2_user_project.sql

# OntologyStore datasource
# ###############################################################################
echo exit | sqlplus i2b2pm/'demouser'@localhost:1521/FREEPDB1 @${DIR}/ontstore_i2b2pm.sql

# i2b2
# ###############################################################################
echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/i2b2hive.sql

# Test Cases
# ###############################################################################
# ================================================================================
# test case 1
# ================================================================================
# echo exit | sqlplus i2b2actata/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_1/additional_i2b2actdata_project_tables.sql
# echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_1/ontstore_ds_xml.sql
# echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_1/ontstore_i2b2hive.sql
# ================================================================================
# test case 2
# ================================================================================
# echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_2/ontstore_ds_xml.sql
# echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_2/ontstore_i2b2hive.sql
# ================================================================================
# test case 3
# ================================================================================
echo exit | sqlplus i2b2actata/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_1/additional_i2b2actdata_project_tables.sql
echo exit | sqlplus system/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_3/ontstore_ds_xml.sql
echo exit | sqlplus i2b2hive/'demouser'@localhost:1521/FREEPDB1 @${DIR}/test_case_3/ontstore_i2b2hive.sql
# ================================================================================

exit 0
