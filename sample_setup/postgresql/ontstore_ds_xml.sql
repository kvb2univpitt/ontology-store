-- create datasource users
CREATE USER i2b2ontstoredata WITH PASSWORD 'demouser';
CREATE USER i2b2ontstoremetadata WITH PASSWORD 'demouser';

-- permit connection to the database
-- GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2ontstoredata;
-- GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2ontstoremetadata;

-- permit usage and creation inside the schema
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoredata;
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoremetadata;

-- grant full permissions on selected schema tables
GRANT ALL ON TABLE public.qt_breakdown_path TO i2b2ontstoredata;

GRANT ALL ON TABLE public.table_access TO i2b2ontstoremetadata;
GRANT ALL ON TABLE public.schemes TO i2b2ontstoremetadata;

-- -- grant full permissions on all existing tables
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO i2b2ontstoredata;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO i2b2ontstoremetadata;

-- share all access rights
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoredata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2demodata;
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoremetadata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2metadata;


-- ################################################################################
-- ACT Ontologystore
-- ################################################################################

-- create datasource users
CREATE USER i2b2ontstoreactdata WITH PASSWORD 'demouser';
CREATE USER i2b2ontstoreactmetadata WITH PASSWORD 'demouser';

-- permit connection to the database
-- GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2ontstoreactdata;
-- GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2ontstoreactmetadata;

-- permit usage and creation inside the schema
GRANT ALL PRIVILEGES ON SCHEMA public TO i2b2ontstoreactdata;
GRANT ALL PRIVILEGES ON SCHEMA i2b2actdata TO i2b2ontstoreactmetadata;

-- grant full permissions on selected schema tables
GRANT ALL ON TABLE public.qt_breakdown_path TO i2b2ontstoreactdata;

GRANT ALL ON TABLE i2b2actdata.table_access TO i2b2ontstoreactmetadata;
GRANT ALL ON TABLE i2b2actdata.schemes TO i2b2ontstoreactmetadata;

-- share all access rights
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoreactdata IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO i2b2demodata;
ALTER DEFAULT PRIVILEGES FOR ROLE i2b2ontstoreactmetadata IN SCHEMA i2b2actdata GRANT ALL PRIVILEGES ON TABLES TO i2b2actata;