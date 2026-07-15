-- create crc user
USE i2b2demodata

CREATE LOGIN i2b2ontstoredata WITH PASSWORD = '<YourStrong@Passw0rd>';

CREATE USER i2b2ontstoredata FOR LOGIN i2b2ontstoredata

GRANT CREATE TABLE TO i2b2ontstoredata;
GRANT ALTER,SELECT,INSERT,UPDATE ON SCHEMA :: dbo TO i2b2ontstoredata;

-- create metadata user
USE i2b2metadata
CREATE LOGIN i2b2ontstoremetadata WITH PASSWORD = '<YourStrong@Passw0rd>';

CREATE USER i2b2ontstoremetadata FOR LOGIN i2b2ontstoremetadata

GRANT CREATE TABLE TO i2b2ontstoremetadata;
GRANT ALTER,SELECT,INSERT,UPDATE ON SCHEMA :: dbo TO i2b2ontstoremetadata;


-- create ACT crc user
USE i2b2demodata

CREATE LOGIN i2b2ontstoreactdata WITH PASSWORD = '<YourStrong@Passw0rd>';

CREATE USER i2b2ontstoreactdata FOR LOGIN i2b2ontstoreactdata

GRANT CREATE TABLE TO i2b2ontstoreactdata;
GRANT ALTER,SELECT,INSERT,UPDATE ON SCHEMA :: dbo TO i2b2ontstoreactdata;

-- create ACT metadata user
USE i2b2actata

CREATE LOGIN i2b2ontstoreactmetadata WITH PASSWORD = '<YourStrong@Passw0rd>';

CREATE USER i2b2ontstoreactmetadata FOR LOGIN i2b2ontstoreactmetadata

GRANT CREATE TABLE TO i2b2ontstoreactmetadata;
GRANT ALTER,SELECT,INSERT,UPDATE ON SCHEMA :: dbo TO i2b2ontstoreactmetadata;
