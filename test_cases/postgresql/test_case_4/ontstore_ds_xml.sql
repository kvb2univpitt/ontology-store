-- ################################################################################
-- Test Case 4:
-- Two sets of datasources: One for the Demo project and one for the ACT project.
-- The CRC data is imported in the main (Demo) project schema for both projects.
-- The metadata is imported in separate project schema.
-- ################################################################################

-- create database users for datasources
-- Demo project
CREATE USER i2b2ontstoredata WITH PASSWORD 'demouser';
CREATE USER i2b2ontstoremetadata WITH PASSWORD 'demouser';
-- ACT project
CREATE USER i2b2ontstoreactdata WITH PASSWORD 'demouser';
CREATE USER i2b2ontstoreactmetadata WITH PASSWORD 'demouser';

-- permit usage and creation inside the schema
-- Demo project
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoredata;
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoremetadata;
-- ACT project
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoreactdata;
GRANT ALL PRIVILEGES ON SCHEMA i2b2actdata TO i2b2ontstoreactmetadata;

-- grant full permissions on selected schema tables
-- Demo project
GRANT ALL ON TABLE public.qt_breakdown_path TO i2b2ontstoredata;
GRANT ALL ON TABLE public.table_access TO i2b2ontstoremetadata;
GRANT ALL ON TABLE public.schemes TO i2b2ontstoremetadata;
-- ACT project
GRANT ALL ON TABLE public.qt_breakdown_path TO i2b2ontstoreactdata;
GRANT ALL ON TABLE i2b2actdata.table_access TO i2b2ontstoreactmetadata;
GRANT ALL ON TABLE i2b2actdata.schemes TO i2b2ontstoreactmetadata;

-- share all access rights
-- Demo project
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoredata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2demodata;
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoremetadata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2metadata;
-- ACT project
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoreactdata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2demodata;
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoreactmetadata IN SCHEMA i2b2actdata GRANT ALL PRIVILEGES ON TABLES TO i2b2actata;