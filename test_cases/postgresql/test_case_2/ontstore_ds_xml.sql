-- ################################################################################
-- Test Case 2:
-- One set of datasources for the Demo project and the ACT project.
-- The CRC data is imported in the main (Demo) project schema for both projects.
-- The metadata is imported in separate project schema.
-- ################################################################################

-- create database users for datasources
CREATE USER i2b2ontstoredata WITH PASSWORD 'demouser';
CREATE USER i2b2ontstoremetadata WITH PASSWORD 'demouser';

-- permit usage and creation inside the schema
-- Demo project and ACT project
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoredata;
-- Demo project
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoremetadata;
-- ACT project
GRANT ALL PRIVILEGES ON SCHEMA i2b2actdata TO i2b2ontstoremetadata;

-- grant full permissions on selected schema tables
-- Demo project and ACT project
GRANT ALL ON TABLE public.qt_breakdown_path TO i2b2ontstoredata;
-- Demo project
GRANT ALL ON TABLE public.table_access TO i2b2ontstoremetadata;
GRANT ALL ON TABLE public.schemes TO i2b2ontstoremetadata;
-- ACT project
GRANT ALL ON TABLE i2b2actdata.table_access TO i2b2ontstoremetadata;
GRANT ALL ON TABLE i2b2actdata.schemes TO i2b2ontstoremetadata;

-- share all access rights
-- Demo project and ACT project
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoredata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2demodata;
-- Demo project
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoremetadata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2metadata;
-- ACT project
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoremetadata IN SCHEMA i2b2actdata GRANT ALL PRIVILEGES ON TABLES TO i2b2actata;