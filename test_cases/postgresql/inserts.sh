#!/usr/bin/env sh

DIR=/home/kvb2univpitt/test_cases/postgresql

# ACT Project
# ##############################################################################
# create new DB user and schema
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project_schema.sql
# add tables to project schema
psql postgresql://i2b2actata:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project_tables.sql
# add project description
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2actdata_project.sql

# Add i2b2 Demo User to ACT Project
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2_user_project.sql

# OntologyStore datasource
# ###############################################################################
psql postgresql://i2b2pm:demouser@localhost:5432/i2b2 -f ${DIR}/ontstore_i2b2pm.sql

# i2b2
# ###############################################################################
psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/i2b2hive.sql

# Test Cases
# ###############################################################################
# ================================================================================
# test case 1
# ================================================================================
# psql postgresql://i2b2actata:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_1/additional_i2b2actdata_project_tables.sql
# psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_1/ontstore_ds_xml.sql
# psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_1/ontstore_i2b2hive.sql
# ================================================================================
# test case 2
# ================================================================================
# psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_2/ontstore_ds_xml.sql
# psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_2/ontstore_i2b2hive.sql
# ================================================================================
# test case 3
# ================================================================================
# psql postgresql://i2b2actata:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_3/additional_i2b2actdata_project_tables.sql
# psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_3/ontstore_ds_xml.sql
# psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_3/ontstore_i2b2hive.sql
# ================================================================================
# test case 4
# ================================================================================
psql postgresql://postgres:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_4/ontstore_ds_xml.sql
psql postgresql://i2b2hive:demouser@localhost:5432/i2b2 -f ${DIR}/test_case_4/ontstore_i2b2hive.sql

exit 0
