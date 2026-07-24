-- ################################################################################
-- Test Case 4:
-- Two sets of datasources: One for the Demo project and one for the ACT project.
-- The CRC data is imported in the main (Demo) project schema for both projects.
-- The metadata is imported in separate project schema.
-- ################################################################################

-- create database users for datasources
-- Demo project
CREATE USER i2b2ontstoredata IDENTIFIED BY demouser;
CREATE USER i2b2ontstoremetadata IDENTIFIED BY demouser;
-- ACT project
CREATE USER i2b2ontstoreactdata IDENTIFIED BY demouser;
CREATE USER i2b2ontstoreactmetadata IDENTIFIED BY demouser;

-- permit usage and creation inside the schema
-- Demo project
GRANT ALL PRIVILEGES TO i2b2ontstoredata;
GRANT ALL PRIVILEGES TO i2b2ontstoremetadata;
-- ACT project
GRANT ALL PRIVILEGES TO i2b2ontstoreactdata;
GRANT ALL PRIVILEGES TO i2b2ontstoreactmetadata;

-- grant full permissions on selected schema tables
-- Demo project
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2demodata TO i2b2ontstoredata;
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2metadata TO i2b2ontstoremetadata;
-- ACT project
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2demodata TO i2b2ontstoreactdata;
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2actata TO i2b2ontstoreactmetadata;
