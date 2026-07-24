-- OntologyStore DB lookup tables
-- Demo project
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_entry_date) VALUES ('i2b2demo','/Demo/','ontstore','public','java:/OntologyStoreDataDS','POSTGRESQL','Demo',current_timestamp);
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_entry_date) VALUES ('i2b2demo','Demo/','ontstore','public','java:/OntologyStoreMetadataDS','POSTGRESQL','Metadata',current_timestamp);
-- ACT project
INSERT INTO crc_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_entry_date) VALUES ('i2b2demo','/ACT/','ontstore','public','java:/OntologyStoreDataDS','POSTGRESQL','Demo',current_timestamp);
INSERT INTO ont_db_lookup (c_domain_id,c_project_path,c_owner_id,c_db_fullschema,c_db_datasource,c_db_servertype,c_db_nicename,c_entry_date) VALUES ('i2b2demo','ACT/','ontstore','i2b2actdata','java:/OntologyStoreMetadataDS','POSTGRESQL','Metadata',current_timestamp);
