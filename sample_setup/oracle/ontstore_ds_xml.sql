-- ################################################################################
-- Demo Project Ontologystore
-- ################################################################################

CREATE USER i2b2ontstoredata IDENTIFIED BY demouser;
CREATE USER i2b2ontstoremetadata IDENTIFIED BY demouser;

GRANT ALL PRIVILEGES TO i2b2ontstoredata;
GRANT ALL PRIVILEGES TO i2b2ontstoremetadata;

GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2demodata TO i2b2ontstoredata;
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2metadata TO i2b2ontstoremetadata;

-- ################################################################################
-- ACT Project Ontologystore
-- ################################################################################

CREATE USER i2b2ontstoreactdata IDENTIFIED BY demouser;
CREATE USER i2b2ontstoreactmetadata IDENTIFIED BY demouser;

GRANT ALL PRIVILEGES TO i2b2ontstoreactdata;
GRANT ALL PRIVILEGES TO i2b2ontstoreactmetadata;

GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2demodata TO i2b2ontstoreactdata;
GRANT SELECT ANY TABLE, INSERT ANY TABLE, UPDATE ANY TABLE ON SCHEMA i2b2actata TO i2b2ontstoreactmetadata;
