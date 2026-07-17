-- update ACT ontology datasource path
UPDATE i2b2hive.dbo.ONT_DB_LOOKUP SET C_DB_FULLSCHEMA = 'i2b2actata.dbo' WHERE C_PROJECT_PATH = 'ACT/';
